import api, { downloadBlob } from "./api";
import type { BackendEquipment } from "./equipmentService";
import type { BackendBooking } from "./bookingService";
import type { BackendMaintenance } from "./maintenanceService";

/**
 * Aligned with ReportController:
 *   GET /api/reports/dashboard            -> ReportResponseDTO
 *   GET /api/reports/equipment            -> Equipment[]
 *   GET /api/reports/bookings             -> Booking[]
 *   GET /api/reports/maintenance          -> Maintenance[]
 *   GET /api/reports/today-bookings       -> number
 *   GET /api/reports/month-bookings       -> number
 *   GET /api/reports/weekly-utilization   -> Object[][]  (day, usage)
 *   GET /api/reports/equipment-usage      -> Object[][]  (equipmentId, bookings)
 *   GET /api/reports/export/{equipment|booking|maintenance}/{excel|pdf}
 */

export interface DashboardReport {
  totalEquipment: number;
  availableEquipment: number;
  bookedEquipment: number;
  underMaintenanceEquipment: number;
  totalBookings: number;
  pendingBookings: number;
  approvedBookings: number;
  completedBookings: number;
  totalMaintenance: number;
  pendingMaintenance: number;
  inProgressMaintenance: number;
  completedMaintenance: number;
}

const num = (v: unknown) => Number(v ?? 0);

export const getDashboardReport = () =>
  api.get<Partial<DashboardReport>>("/api/reports/dashboard").then((r) => {
    const d = r.data ?? {};
    return {
      totalEquipment: num(d.totalEquipment),
      availableEquipment: num(d.availableEquipment),
      bookedEquipment: num(d.bookedEquipment),
      underMaintenanceEquipment: num(d.underMaintenanceEquipment),
      totalBookings: num(d.totalBookings),
      pendingBookings: num(d.pendingBookings),
      approvedBookings: num(d.approvedBookings),
      completedBookings: num(d.completedBookings),
      totalMaintenance: num(d.totalMaintenance),
      pendingMaintenance: num(d.pendingMaintenance),
      inProgressMaintenance: num(d.inProgressMaintenance),
      completedMaintenance: num(d.completedMaintenance),
    } satisfies DashboardReport;
  });

export const getEquipmentReport = () =>
  api.get<BackendEquipment[]>("/api/reports/equipment").then((r) => r.data);

export const getBookingReport = () =>
  api.get<BackendBooking[]>("/api/reports/bookings").then((r) => r.data);

export const getMaintenanceReport = () =>
  api.get<BackendMaintenance[]>("/api/reports/maintenance").then((r) => r.data);

export const getTodayBookings = () =>
  api.get<number>("/api/reports/today-bookings").then((r) => num(r.data));

export const getCurrentMonthBookings = () =>
  api.get<number>("/api/reports/month-bookings").then((r) => num(r.data));

export const getWeeklyUtilizationReport = () =>
  api.get<(string | number)[][]>("/api/reports/weekly-utilization").then((r) =>
    (r.data ?? []).map((row) => ({ day: String(row[0]).trim(), usage: num(row[1]) })),
  );

export const getEquipmentUsageReport = () =>
  api.get<(string | number)[][]>("/api/reports/equipment-usage").then((r) =>
    (r.data ?? []).map((row) => ({ equipmentId: num(row[0]), bookings: num(row[1]) })),
  );

export type ExportEntity = "equipment" | "booking" | "maintenance";
export type ExportFormat = "excel" | "pdf";

const EXT: Record<ExportFormat, string> = { excel: "xlsx", pdf: "pdf" };

/** Downloads the export file directly. */
export async function exportReportFile(entity: ExportEntity, format: ExportFormat) {
  const res = await api.get(`/api/reports/export/${entity}/${format}`, { responseType: "blob" });
  downloadBlob(res.data as Blob, `${entity}-report.${EXT[format]}`);
}

/** CSV is generated client-side from the same backend report data (no CSV endpoint exists). */
export async function exportReportCsv(entity: ExportEntity) {
  let rows: Record<string, unknown>[] = [];
  if (entity === "equipment") rows = (await getEquipmentReport()) as unknown as Record<string, unknown>[];
  else if (entity === "booking") rows = (await getBookingReport()) as unknown as Record<string, unknown>[];
  else rows = (await getMaintenanceReport()) as unknown as Record<string, unknown>[];

  if (rows.length === 0) {
    downloadBlob(new Blob([""], { type: "text/csv" }), `${entity}-report.csv`);
    return;
  }
  const headers = Object.keys(rows[0]);
  const escape = (v: unknown) => {
    const s = v === null || v === undefined ? "" : String(v);
    return /[",\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s;
  };
  const csv = [headers.join(","), ...rows.map((r) => headers.map((h) => escape(r[h])).join(","))].join("\n");
  downloadBlob(new Blob([csv], { type: "text/csv;charset=utf-8" }), `${entity}-report.csv`);
}

export { downloadBlob };
