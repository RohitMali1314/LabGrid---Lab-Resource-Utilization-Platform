import api from "./api";

/**
 * Aligned with NotificationController:
 *   POST   /api/notifications
 *   GET    /api/notifications                       (admins)
 *   GET    /api/notifications/{id}
 *   GET    /api/notifications/user/{userId}
 *   GET    /api/notifications/user/{userId}/unread
 *   GET    /api/notifications/user/{userId}/count
 *   PUT    /api/notifications/{id}/read
 *   DELETE /api/notifications/{id}                  (admins)
 */

export interface BackendNotification {
  notificationId: number;
  userId: number;
  title: string;
  message?: string;
  notificationType?: string;
  referenceId?: number;
  isRead?: boolean;
  createdAt?: string;
}

export interface Notification extends BackendNotification {
  /** UI aliases */
  id: number;
  read: boolean;
  type?: string;
}

const normalize = (n: BackendNotification): Notification => ({
  ...n,
  id: n.notificationId,
  read: Boolean(n.isRead),
  type: n.notificationType,
});

export const listAllNotifications = () =>
  api.get<BackendNotification[]>("/api/notifications").then((r) => r.data.map(normalize));

export const listUserNotifications = (userId: number) =>
  api.get<BackendNotification[]>(`/api/notifications/user/${userId}`).then((r) => r.data.map(normalize));

export const listUnreadNotifications = (userId: number) =>
  api
    .get<BackendNotification[]>(`/api/notifications/user/${userId}/unread`)
    .then((r) => r.data.map(normalize));

export const getUnreadCount = (userId: number) =>
  api.get<number>(`/api/notifications/user/${userId}/count`).then((r) => Number(r.data ?? 0));

export const markNotificationRead = (id: number) =>
  api.put<BackendNotification>(`/api/notifications/${id}/read`, {}).then((r) => normalize(r.data));

export const deleteNotification = (id: number) =>
  api.delete<string>(`/api/notifications/${id}`).then((r) => r.data);

export const createNotification = (payload: {
  userId: number;
  title: string;
  message?: string;
  notificationType?: string;
  referenceId?: number;
}) => api.post<BackendNotification>("/api/notifications", payload).then((r) => normalize(r.data));
