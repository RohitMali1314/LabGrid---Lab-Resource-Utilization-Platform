import axios, { AxiosError, type AxiosInstance, type InternalAxiosRequestConfig } from "axios";

export const API_BASE_URL =
  (import.meta.env.VITE_API_BASE_URL as string | undefined) || "http://localhost:8080";

export const TOKEN_KEY = "lab_token";
export const USER_KEY = "lab_user";

const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: { "Content-Type": "application/json" },
  timeout: 20000,
});

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = typeof window !== "undefined" ? localStorage.getItem(TOKEN_KEY) : null;
  if (token) {
    config.headers.set("Authorization", `Bearer ${token}`);
  }
  return config;
});

let onUnauthorized: (() => void) | null = null;
export function setUnauthorizedHandler(fn: () => void) {
  onUnauthorized = fn;
}

let onForbidden: ((message: string) => void) | null = null;
export function setForbiddenHandler(fn: (message: string) => void) {
  onForbidden = fn;
}

api.interceptors.response.use(
  (r) => r,
  (error: AxiosError<{ message?: string; error?: string }>) => {
    const status = error.response?.status;
    if (status === 401) {
      if (typeof window !== "undefined") {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
      }
      onUnauthorized?.();
      // Session expired — send the user back to login.
      if (typeof window !== "undefined" && !window.location.pathname.startsWith("/login")) {
        window.location.assign("/login");
      }
    } else if (status === 403) {
      onForbidden?.("Access denied. You don't have permission for this action.");
    }
    return Promise.reject(error);
  },
);

export type ApiErrorKind =
  | "auth"
  | "forbidden"
  | "unavailable"
  | "validation"
  | "conflict"
  | "server"
  | "network"
  | "unknown";

/** Classify any error so the UI can pick the right (non-technical) state. */
export function apiErrorKind(err: unknown): ApiErrorKind {
  if (!axios.isAxiosError(err)) return "unknown";
  if (!err.response) return "network";
  const s = err.response.status;
  if (s === 401) return "auth";
  if (s === 403) return "forbidden";
  if (s === 404) return "unavailable";
  if (s === 400 || s === 422) return "validation";
  if (s === 409) return "conflict";
  if (s >= 500) return "server";
  return "unknown";
}

/** Human-readable, never-technical message for any error. */
export function apiErrorMessage(err: unknown, fallback = "Something went wrong"): string {
  if (axios.isAxiosError(err)) {
    if (!err.response) return "Unable to connect to the server. Please try again later.";
    const data = err.response.data as { message?: string; error?: string; errors?: unknown } | undefined;
    const status = err.response.status;
    if (status === 400 || status === 422) {
      if (data && typeof data === "object" && data.errors && typeof data.errors === "object") {
        const parts = Object.values(data.errors as Record<string, string>);
        if (parts.length) return parts.join(" · ");
      }
      return clean(data?.message) || clean(data?.error) || "Please check the submitted values.";
    }
    if (status === 401) return "Your session has expired. Please log in again.";
    if (status === 403) return "You don't have permission to access this feature.";
    if (status === 404) return "This feature is currently unavailable. Backend integration is pending.";
    if (status === 409) return clean(data?.message) || "This time slot is no longer available.";
    if (status >= 500) return "Something went wrong while loading this data. Please try again later.";
    return clean(data?.message) || clean(data?.error) || fallback;
  }
  return fallback;
}

/** Drop anything that looks like a stack trace / Java exception before display. */
function clean(msg?: string): string | undefined {
  if (!msg) return undefined;
  const trimmed = msg.trim();
  if (!trimmed) return undefined;
  if (/(\bException\b|\bat [a-z]+\.[A-Za-z]|org\.springframework|java\.lang|SQL|AxiosError|Error: )/.test(trimmed))
    return undefined;
  if (trimmed.length > 200) return undefined;
  return trimmed;
}


/** Trigger a browser download for a binary/blob response. */
export function downloadBlob(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}

export default api;
