import { getEquipmentHeatmapData, getDepartmentStats } from "./dashboardService";
import { listBookings } from "./bookingService";
import { getMonthlyHeatmap } from "./analyticsService";

/**
 * Heatmaps.
 * Equipment / department intensity comes straight from the backend:
 *   GET /api/dashboard/heatmap
 *   GET /api/dashboard/department-statistics
 * Temporal (hour x day, weekday x week) matrices are aggregated from the real
 * booking records returned by GET /api/bookings.
 */

export interface HeatmapCell {
  x: string;
  y: string;
  value: number;
}

const DAY_NAMES = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
const MONTH_NAMES = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"];

const pad = (n: number) => String(n).padStart(2, "0");

export async function getHourlyHeatmap(): Promise<HeatmapCell[]> {
  const bookings = await listBookings();
  const counts = new Map<string, number>();
  for (const b of bookings) {
    const d = new Date(b.startTime);
    if (isNaN(d.getTime())) continue;
    const key = `${DAY_NAMES[d.getDay()]}|${pad(d.getHours())}:00`;
    counts.set(key, (counts.get(key) ?? 0) + 1);
  }
  return Array.from(counts.entries()).map(([key, value]) => {
    const [x, y] = key.split("|");
    return { x, y, value };
  });
}

export async function getDailyHeatmap(): Promise<HeatmapCell[]> {
  const bookings = await listBookings();
  const counts = new Map<string, number>();
  for (const b of bookings) {
    const d = new Date(b.startTime);
    if (isNaN(d.getTime())) continue;
    const week = `Week ${Math.ceil(d.getDate() / 7)}`;
    const key = `${DAY_NAMES[d.getDay()]}|${week}`;
    counts.set(key, (counts.get(key) ?? 0) + 1);
  }
  return Array.from(counts.entries()).map(([key, value]) => {
    const [x, y] = key.split("|");
    return { x, y, value };
  });
}

export async function getEquipmentHeatmap(): Promise<HeatmapCell[]> {
  const rows = await getEquipmentHeatmapData();
  return rows.map((r) => ({
    x: "Utilisation",
    y: r.equipmentName ?? `#${r.equipmentId}`,
    value: Math.round(r.utilization ?? 0),
  }));
}

export async function getDepartmentHeatmap(): Promise<HeatmapCell[]> {
  const rows = await getDepartmentStats();
  return rows.map((r) => ({ x: "Bookings", y: r.name, value: Number(r.value ?? 0) }));
}

/** Booking density: equipment (y) x hour of day (x), derived from real bookings. */
export async function getBookingDensityHeatmap(): Promise<HeatmapCell[]> {
  const [bookings, heat] = await Promise.all([listBookings(), getEquipmentHeatmapData()]);
  const names = new Map(heat.map((h) => [h.equipmentId, h.equipmentName]));
  const counts = new Map<string, number>();
  for (const b of bookings) {
    const d = new Date(b.startTime);
    if (isNaN(d.getTime())) continue;
    const key = `${pad(d.getHours())}:00|${names.get(b.equipmentId) ?? `#${b.equipmentId}`}`;
    counts.set(key, (counts.get(key) ?? 0) + 1);
  }
  return Array.from(counts.entries()).map(([key, value]) => {
    const [x, y] = key.split("|");
    return { x, y, value };
  });
}

/** GET /api/analytics/heatmap/monthly -> day (x) x month (y) matrix. */
export async function getMonthlyHeatmapCells(): Promise<HeatmapCell[]> {
  const rows = await getMonthlyHeatmap();
  return rows.map((r) => ({
    x: String(r.day),
    y: MONTH_NAMES[(r.month - 1 + 12) % 12] ?? `M${r.month}`,
    value: r.bookings,
  }));
}
