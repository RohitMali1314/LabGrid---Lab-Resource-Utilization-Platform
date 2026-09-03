import api from "./api";

/**
 * Aligned with AnalyticsController:
 *   GET /api/analytics/utilization        -> UtilizationPointDTO[]
 *   GET /api/analytics/heatmap/monthly    -> Object[][]  (month, day, bookings)
 *   GET /api/analytics/departments        -> DepartmentStatDTO[]
 */

export interface UtilizationPoint {
  day: string;
  usage: number;
}

export interface DepartmentStat {
  name: string;
  value: number;
}

export interface MonthlyHeatPoint {
  month: number;
  day: number;
  bookings: number;
}

export const getAnalyticsUtilization = () =>
  api.get<UtilizationPoint[]>("/api/analytics/utilization").then((r) =>
    r.data.map((p) => ({ day: p.day, usage: Number(p.usage ?? 0) })),
  );

export const getAnalyticsDepartments = () =>
  api.get<DepartmentStat[]>("/api/analytics/departments").then((r) =>
    r.data.map((d) => ({ name: d.name, value: Number(d.value ?? 0) })),
  );

export const getMonthlyHeatmap = () =>
  api.get<(string | number)[][]>("/api/analytics/heatmap/monthly").then((r) =>
    (r.data ?? []).map((row) => ({
      month: Number(row[0]),
      day: Number(row[1]),
      bookings: Number(row[2]),
    })) as MonthlyHeatPoint[],
  );
