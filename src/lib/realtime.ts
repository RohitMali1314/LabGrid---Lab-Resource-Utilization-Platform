import { Client, type IMessage } from "@stomp/stompjs";
import { API_BASE_URL, TOKEN_KEY } from "@/services/api";

/** Payload pushed by the backend on /topic/notifications/{userId}. */
export interface RealtimeNotification {
  userId?: number;
  title?: string;
  message?: string;
  type?: string;
  time?: string;
}

/**
 * sockjs-client (and its transitive deps) reference the Node `global` object.
 * Vite does not define it in the browser, which throws "global is not defined".
 * We alias it to `globalThis` before the module is loaded.
 */
function ensureGlobal() {
  const g = globalThis as unknown as { global?: unknown };
  if (typeof g.global === "undefined") g.global = globalThis;
}

/** Loads the browser build of SockJS lazily (never during SSR). */
async function loadSockJS(): Promise<new (url: string) => WebSocket> {
  ensureGlobal();
  const mod = await import("sockjs-client/dist/sockjs.js");
  const SockJS = (mod as { default?: unknown }).default ?? mod;
  return SockJS as unknown as new (url: string) => WebSocket;
}

/**
 * Connects to the Spring STOMP endpoint (/ws, SockJS) and subscribes to
 * /topic/notifications/{userId}. Returns a disconnect function.
 */
export function connectNotifications(
  userId: number,
  onMessage: (n: RealtimeNotification) => void,
): () => void {
  if (typeof window === "undefined") return () => {};

  let client: Client | null = null;
  let cancelled = false;
  const token = localStorage.getItem(TOKEN_KEY);

  void loadSockJS()
    .then((SockJS) => {
      if (cancelled) return;
      client = new Client({
        webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws`),
        connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
        reconnectDelay: 5000,
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,
        debug: () => {},
        onConnect: () => {
          client?.subscribe(`/topic/notifications/${userId}`, (frame: IMessage) => {
            try {
              onMessage(JSON.parse(frame.body) as RealtimeNotification);
            } catch {
              onMessage({ title: frame.body });
            }
          });
        },
        onStompError: () => {},
        onWebSocketError: () => {},
      });
      client.activate();
    })
    .catch(() => {
      /* real-time is best-effort; polling still refreshes notifications */
    });

  return () => {
    cancelled = true;
    void client?.deactivate();
  };
}
