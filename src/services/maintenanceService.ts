import api from "./api";

/**
 * Aligned with MaintenanceController + MaintenanceRequestDTO / MaintenanceResponseDTO.
 *   POST /api/maintenance
 *   GET  /api/maintenance | /{id} | /equipment/{id} | /technician/{id} | /status/{s} | /priority/{p}
 *   GET  /api/maintenance/calibration/pending
 *   PUT  /api/maintenance/{id}/start
 *   PUT  /api/maintenance/{id}/complete?remarks=
 *   GET  /api/maintenance/dashboard/{total|pending|in-progress|completed|preventive|corrective|calibration}
 */

export type MaintenanceStatus = "PENDING" | "IN_PROGRESS" | "COMPLETED";
export type MaintenancePriority = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";

export interface BackendMaintenance {
  maintenanceId: number;
  equipmentId: number;
  technicianId?: number;
  reportedBy?: number;
  workOrderNumber?: string;
  issue?: string;
  issueDescription?: string;
  maintenanceType?: string;
  priority?: string;
  status: string;
  scheduledDate?: string;
  startedDate?: string;
  estimatedCompletionDate?: string;
  completedDate?: string;
  downtimeHours?: number;
  calibrationRequired?: boolean;
  calibrationDate?: string;
  nextCalibrationDate?: string;
  certificateNumber?: string;
  remarks?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface MaintenanceRequest extends BackendMaintenance {
  /** UI alias */
  id: number;
}

export interface CreateMaintenancePayload {
  equipmentId: number;
  technicianId?: number | null;
  reportedBy?: number | null;
  issue?: string;
  issueDescription?: string;
  maintenanceType?: string;
  priority?: string;
  scheduledDate?: string | null;
  estimatedCompletionDate?: string | null;
  calibrationRequired?: boolean;
  calibrationDate?: string | null;
  nextCalibrationDate?: string | null;
  certificateNumber?: string | null;
  remarks?: string | null;
}

export interface MaintenanceCounters {
  total: number;
  pending: number;
  inProgress: number;
  completed: number;
  preventive: number;
  corrective: number;
  calibration: number;
}

export const MAINTENANCE_TYPES = ["PREVENTIVE", "CORRECTIVE", "CALIBRATION", "BREAKDOWN"];
export const MAINTENANCE_PRIORITIES: MaintenancePriority[] = ["LOW", "MEDIUM", "HIGH", "CRITICAL"];

const normalize = (m: BackendMaintenance): MaintenanceRequest => ({ ...m, id: m.maintenanceId });

export const listMaintenance = () =>
  api.get<BackendMaintenance[]>("/api/maintenance").then((r) => r.data.map(normalize));

export const getMaintenance = (id: number) =>
  api.get<BackendMaintenance>(`/api/maintenance/${id}`).then((r) => normalize(r.data));

export const listMaintenanceByEquipment = (equipmentId: number) =>
  api
    .get<BackendMaintenance[]>(`/api/maintenance/equipment/${equipmentId}`)
    .then((r) => r.data.map(normalize));

export const listMaintenanceByTechnician = (technicianId: number) =>
  api
    .get<BackendMaintenance[]>(`/api/maintenance/technician/${technicianId}`)
    .then((r) => r.data.map(normalize));

export const listMaintenanceByStatus = (status: string) =>
  api.get<BackendMaintenance[]>(`/api/maintenance/status/${status}`).then((r) => r.data.map(normalize));

export const listMaintenanceByPriority = (priority: string) =>
  api
    .get<BackendMaintenance[]>(`/api/maintenance/priority/${priority}`)
    .then((r) => r.data.map(normalize));

/** Calibration workflow — records whose next calibration is due. */
export const listPendingCalibrations = () =>
  api.get<BackendMaintenance[]>("/api/maintenance/calibration/pending").then((r) => r.data.map(normalize));

export const createMaintenance = (payload: CreateMaintenancePayload) =>
  api.post<BackendMaintenance>("/api/maintenance", payload).then((r) => normalize(r.data));

export const startMaintenance = (id: number) =>
  api.put<BackendMaintenance>(`/api/maintenance/${id}/start`, {}).then((r) => normalize(r.data));

export const completeMaintenance = (id: number, remarks?: string) =>
  api
    .put<BackendMaintenance>(`/api/maintenance/${id}/complete`, {}, {
      params: remarks ? { remarks } : undefined,
    })
    .then((r) => normalize(r.data));

const counter = (path: string) =>
  api.get<number>(`/api/maintenance/dashboard/${path}`).then((r) => Number(r.data ?? 0));

export const getMaintenanceCounters = async (): Promise<MaintenanceCounters> => {
  const [total, pending, inProgress, completed, preventive, corrective, calibration] =
    await Promise.all([
      counter("total"),
      counter("pending"),
      counter("in-progress"),
      counter("completed"),
      counter("preventive"),
      counter("corrective"),
      counter("calibration"),
    ]);
  return { total, pending, inProgress, completed, preventive, corrective, calibration };
};
