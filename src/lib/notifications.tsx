import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from "react";
import { toast } from "sonner";
import { useAuth } from "@/lib/auth";
import { connectNotifications } from "@/lib/realtime";
import {
  deleteNotification as apiDelete,
  getUnreadCount,
  listAllNotifications,
  listUnreadNotifications,
  listUserNotifications,
  markNotificationRead as apiMarkRead,
  type Notification,
} from "@/services/notificationService";

interface NotificationsCtx {
  all: Notification[];
  mine: Notification[];
  unread: Notification[];
  unreadCount: number;
  loading: boolean;
  error: string | null;
  canSeeAll: boolean;
  refresh: () => Promise<void>;
  markRead: (id: number) => Promise<void>;
  markAllRead: () => Promise<void>;
  remove: (id: number) => Promise<void>;
}

const Ctx = createContext<NotificationsCtx | null>(null);

const ADMIN_ROLES = ["SYSTEM_ADMIN", "INSTITUTION_ADMIN"];
const POLL_MS = 60_000;

export function NotificationsProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const [all, setAll] = useState<Notification[]>([]);
  const [mine, setMine] = useState<Notification[]>([]);
  const [unread, setUnread] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const userId = user?.id ?? null;
  const canSeeAll = !!user && ADMIN_ROLES.includes(user.role);
  const busy = useRef(false);

  const refresh = useCallback(async () => {
    if (!userId || busy.current) return;
    busy.current = true;
    setLoading(true);
    setError(null);
    try {
      const [m, u, c] = await Promise.all([
        listUserNotifications(userId).catch(() => [] as Notification[]),
        listUnreadNotifications(userId).catch(() => [] as Notification[]),
        getUnreadCount(userId).catch(() => 0),
      ]);
      setMine(m);
      setUnread(u);
      setUnreadCount(c || u.length);
      if (canSeeAll) setAll(await listAllNotifications().catch(() => []));
    } catch {
      setError("Unable to load notifications.");
    } finally {
      setLoading(false);
      busy.current = false;
    }
  }, [userId, canSeeAll]);

  useEffect(() => {
    if (!userId) return;
    void refresh();
    const t = setInterval(() => void refresh(), POLL_MS);
    return () => clearInterval(t);
  }, [userId, refresh]);

  // Real-time push over STOMP/SockJS.
  useEffect(() => {
    if (!userId) return;
    const disconnect = connectNotifications(userId, (n) => {
      toast(n.title || "New notification", { description: n.message });
      setUnreadCount((c) => c + 1);
      void refresh();
    });
    return disconnect;
  }, [userId, refresh]);

  const markRead = useCallback(
    async (id: number) => {
      await apiMarkRead(id);
      await refresh();
    },
    [refresh],
  );

  const markAllRead = useCallback(async () => {
    await Promise.all(unread.map((n) => apiMarkRead(n.id).catch(() => null)));
    await refresh();
  }, [unread, refresh]);

  const remove = useCallback(
    async (id: number) => {
      await apiDelete(id);
      await refresh();
    },
    [refresh],
  );

  const value = useMemo(
    () => ({ all, mine, unread, unreadCount, loading, error, canSeeAll, refresh, markRead, markAllRead, remove }),
    [all, mine, unread, unreadCount, loading, error, canSeeAll, refresh, markRead, markAllRead, remove],
  );

  return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
}

export function useNotifications() {
  const c = useContext(Ctx);
  if (!c) throw new Error("useNotifications must be used within NotificationsProvider");
  return c;
}
