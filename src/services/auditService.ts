import api from "./api";

/**
 * Aligned with AuditLogController (SYSTEM_ADMIN only):
 *   GET /api/audit
 *   GET /api/audit/user/{userId}
 *   GET /api/audit/module/{module}
 *   GET /api/audit/action/{action}
 */

export interface AuditLog {
  auditId: number;
  userId?: number;
  action?: string;
  module?: string;
  description?: string;
  ipAddress?: string;
  createdAt?: string;
}

export const listAuditLogs = () => api.get<AuditLog[]>("/api/audit").then((r) => r.data);

export const listAuditLogsByUser = (userId: number) =>
  api.get<AuditLog[]>(`/api/audit/user/${userId}`).then((r) => r.data);

export const listAuditLogsByModule = (module: string) =>
  api.get<AuditLog[]>(`/api/audit/module/${module}`).then((r) => r.data);

export const listAuditLogsByAction = (action: string) =>
  api.get<AuditLog[]>(`/api/audit/action/${action}`).then((r) => r.data);
