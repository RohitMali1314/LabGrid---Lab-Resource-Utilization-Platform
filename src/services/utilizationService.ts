/**
 * MILESTONE 2 — PARTIALLY IMPLEMENTED IN BACKEND.
 * Implemented: /api/analytics/utilization (weekly), /api/equipment/dashboard.
 * TODO(backend): add real-time utilization endpoints.
 *   Suggested endpoints:
 *     GET /api/utilization/live               -> per-equipment status snapshot
 *     GET /api/utilization/equipment/{id}     -> historical utilization %
 *     GET /api/utilization/department/{id}
 *     GET /api/utilization/institution/{id}
 *     GET /api/utilization/summary            -> { avgUsageHours, idleHours, downtimeHours, capacityUsage }
 */

import { getEquipmentDashboardCounts, listEquipment } from "./equipmentService";
import { listBookingsForRole } from "./bookingService";
import type { Role } from "@/lib/auth";
import { getUtilizationSeries } from "./dashboardService";

export interface LiveUtilizationSummary {
  totalEquipment: number;
  availableEquipment: number;
  bookedEquipment: number;
  underMaintenanceEquipment: number;
  activeBookings: number;
  runningSessions: number;
  liveUtilizationPct: number;
}

/** Derived on the frontend from existing endpoints until a dedicated one exists. */
export async function getLiveUtilization(role?: Role | null): Promise<LiveUtilizationSummary> {
  const [counts, bookings, equipment] = await Promise.all([
    getEquipmentDashboardCounts().catch(() => null),
    listBookingsForRole(role).catch(() => []),
    listEquipment().catch(() => []),
  ]);

  const totalEquipment = counts?.totalEquipment ?? equipment.length;
  const availableEquipment = counts?.availableEquipment ?? 0;
  const bookedEquipment = counts?.bookedEquipment ?? 0;
  const underMaintenanceEquipment = counts?.underMaintenanceEquipment ?? 0;
  const activeBookings = bookings.filter((b) => b.status === "APPROVED" || b.status === "IN_USE").length;
  const runningSessions = bookings.filter((b) => b.status === "IN_USE").length;
  const liveUtilizationPct = totalEquipment
    ? Math.min(100, Math.round(((bookedEquipment + runningSessions) / totalEquipment) * 100))
    : 0;

  return {
    totalEquipment,
    availableEquipment,
    bookedEquipment,
    underMaintenanceEquipment,
    activeBookings,
    runningSessions,
    liveUtilizationPct,
  };
}

export { getUtilizationSeries };
