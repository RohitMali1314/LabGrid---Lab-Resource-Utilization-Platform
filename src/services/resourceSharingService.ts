import api from "./api";

/**
 * Backend: ResourceShareController — /api/resource-sharing
 * Only the endpoints that actually exist are exposed here.
 */

export type ShareStatus = "PENDING" | "APPROVED" | "REJECTED" | "ACTIVE" | "CANCELLED";

export interface ResourceShare {
  shareId: number;
  equipmentId: number;
  ownerInstitutionId: number;
  targetInstitutionId: number;
  requestedBy?: number;
  approvedBy?: number;
  status: ShareStatus;
  startDate?: string;
  endDate?: string;
  sharingRate?: number;
  remarks?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateResourceSharePayload {
  equipmentId: number;
  targetInstitutionId: number;
  startDate: string; // yyyy-MM-dd
  endDate: string; // yyyy-MM-dd
  sharingRate?: number;
  remarks?: string;
}

export const listResourceShares = () =>
  api.get<ResourceShare[]>("/api/resource-sharing").then((r) => r.data ?? []);

export const getResourceShare = (id: number) =>
  api.get<ResourceShare>(`/api/resource-sharing/${id}`).then((r) => r.data);

export const listSharesByEquipment = (equipmentId: number) =>
  api.get<ResourceShare[]>(`/api/resource-sharing/equipment/${equipmentId}`).then((r) => r.data ?? []);

export const listSharesByOwner = (institutionId: number) =>
  api.get<ResourceShare[]>(`/api/resource-sharing/owner/${institutionId}`).then((r) => r.data ?? []);

export const listSharesByTarget = (institutionId: number) =>
  api.get<ResourceShare[]>(`/api/resource-sharing/target/${institutionId}`).then((r) => r.data ?? []);

export const listSharesByStatus = (status: ShareStatus) =>
  api.get<ResourceShare[]>(`/api/resource-sharing/status/${status}`).then((r) => r.data ?? []);

export const createResourceShare = (payload: CreateResourceSharePayload) =>
  api.post<ResourceShare>("/api/resource-sharing", payload).then((r) => r.data);

export const approveResourceShare = (id: number) =>
  api.put<ResourceShare>(`/api/resource-sharing/${id}/approve`, {}).then((r) => r.data);

export const rejectResourceShare = (id: number, remarks?: string) =>
  api
    .put<ResourceShare>(
      `/api/resource-sharing/${id}/reject${remarks ? `?remarks=${encodeURIComponent(remarks)}` : ""}`,
      {},
    )
    .then((r) => r.data);

export const activateResourceShare = (id: number) =>
  api.put<ResourceShare>(`/api/resource-sharing/${id}/activate`, {}).then((r) => r.data);

export const cancelResourceShare = (id: number) =>
  api.put<ResourceShare>(`/api/resource-sharing/${id}/cancel`, {}).then((r) => r.data);
