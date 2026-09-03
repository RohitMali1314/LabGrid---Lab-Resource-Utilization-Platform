import { createFileRoute } from "@tanstack/react-router";
import { Area, AreaChart, Bar, BarChart, CartesianGrid, Cell, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { toast } from "sonner";
import { PageHeader, StatCard } from "@/components/page-header";
import { ChartCard } from "@/components/chart-card";
import { Button } from "@/components/ui/button";
import { LoadingState, ErrorState, EmptyState } from "@/components/async-state";
import { useApi } from "@/hooks/use-api";
import { getDepartmentStats, type DepartmentStat } from "@/services/dashboardService";
import {
  exportReportCsv,
  exportReportFile,
  getCurrentMonthBookings,
  getDashboardReport,
  getEquipmentUsageReport,
  getTodayBookings,
  getWeeklyUtilizationReport,
  type DashboardReport,
  type ExportEntity,
  type ExportFormat,
} from "@/services/reportService";
import { listEquipment, type Equipment } from "@/services/equipmentService";
import { apiErrorMessage } from "@/services/api";
import { CalendarDays, CalendarRange, Download, FileSpreadsheet, FileText } from "lucide-react";

export const Route = createFileRoute("/_app/reports")({ component: ReportsPage });

const COLORS = ["var(--color-chart-1)", "var(--color-chart-2)", "var(--color-chart-3)", "var(--color-chart-4)"];

const ENTITIES: ExportEntity[] = ["equipment", "booking", "maintenance"];

function ReportsPage() {
  const summary = useApi<DashboardReport>(getDashboardReport, []);
  const util = useApi(getWeeklyUtilizationReport, []);
  const usage = useApi(getEquipmentUsageReport, []);
  const depts = useApi<DepartmentStat[]>(getDepartmentStats, []);
  const today = useApi<number>(getTodayBookings, []);
  const month = useApi<number>(getCurrentMonthBookings, []);
  const equipment = useApi<Equipment[]>(listEquipment, []);

  const names = new Map((equipment.data ?? []).map((e) => [e.equipmentId, e.equipmentName]));
  const usageData = (usage.data ?? []).map((u) => ({
    name: names.get(u.equipmentId) ?? `#${u.equipmentId}`,
    bookings: u.bookings,
  }));

  const doExport = async (entity: ExportEntity, format: ExportFormat | "csv") => {
    try {
      if (format === "csv") await exportReportCsv(entity);
      else await exportReportFile(entity, format);
      toast.success(`${entity} ${format.toUpperCase()} downloaded`);
    } catch (err) {
      toast.error(apiErrorMessage(err, "Export failed"));
    }
  };

  const s = summary.data;

  return (
    <div className="space-y-6">
      <PageHeader title="Reports & Analytics" description="Operational reporting, utilization insights and exports." />

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard label="Today's Bookings" value={today.loading ? "…" : (today.data ?? 0)} icon={<CalendarDays className="h-4 w-4" />} />
        <StatCard label="This Month" value={month.loading ? "…" : (month.data ?? 0)} icon={<CalendarRange className="h-4 w-4" />} />
        <StatCard label="Total Equipment" value={summary.loading ? "…" : (s?.totalEquipment ?? 0)} hint={s ? `${s.availableEquipment} available` : undefined} />
        <StatCard label="Total Maintenance" value={summary.loading ? "…" : (s?.totalMaintenance ?? 0)} hint={s ? `${s.pendingMaintenance} pending` : undefined} />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <ChartCard title="Weekly Utilization" description="From /api/reports/weekly-utilization">
          {util.loading ? <LoadingState /> : util.error ? <ErrorState message={util.error} onRetry={util.reload} /> :
            !util.data || util.data.length === 0 ? <EmptyState title="No utilization data" /> : (
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={util.data}>
                  <defs>
                    <linearGradient id="rp" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="var(--color-chart-1)" stopOpacity={0.6} />
                      <stop offset="95%" stopColor="var(--color-chart-1)" stopOpacity={0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" opacity={0.3} />
                  <XAxis dataKey="day" fontSize={12} />
                  <YAxis fontSize={12} />
                  <Tooltip />
                  <Area type="monotone" dataKey="usage" stroke="var(--color-chart-1)" fill="url(#rp)" strokeWidth={2} />
                </AreaChart>
              </ResponsiveContainer>
            )}
        </ChartCard>
        <ChartCard title="Department Share">
          {depts.loading ? <LoadingState /> : depts.error ? <ErrorState message={depts.error} onRetry={depts.reload} /> :
            !depts.data || depts.data.length === 0 ? <EmptyState title="No department data" /> : (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={depts.data} dataKey="value" nameKey="name" innerRadius={60} outerRadius={90} paddingAngle={2}>
                    {depts.data.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            )}
        </ChartCard>
      </div>

      <ChartCard title="Equipment Usage" description="Bookings per equipment · /api/reports/equipment-usage">
        {usage.loading ? <LoadingState /> : usage.error ? <ErrorState message={usage.error} onRetry={usage.reload} /> :
          usageData.length === 0 ? <EmptyState title="No usage data" /> : (
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={usageData}>
                <CartesianGrid strokeDasharray="3 3" opacity={0.3} />
                <XAxis dataKey="name" fontSize={11} interval={0} angle={-15} textAnchor="end" height={60} />
                <YAxis fontSize={12} />
                <Tooltip />
                <Bar dataKey="bookings" fill="var(--color-chart-2)" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
      </ChartCard>

      <div className="rounded-xl border bg-card">
        <div className="border-b p-4">
          <h3 className="font-semibold">Exports</h3>
          <p className="text-xs text-muted-foreground">
            Excel and PDF are generated by the backend; CSV is built from the same report data.
          </p>
        </div>
        <div className="divide-y">
          {ENTITIES.map((entity) => (
            <div key={entity} className="flex flex-col gap-3 p-4 sm:flex-row sm:items-center sm:justify-between">
              <div className="text-sm font-medium capitalize">{entity} report</div>
              <div className="flex flex-wrap gap-2">
                <Button size="sm" variant="outline" onClick={() => doExport(entity, "csv")}>
                  <Download className="mr-2 h-3 w-3" /> CSV
                </Button>
                <Button size="sm" variant="outline" onClick={() => doExport(entity, "excel")}>
                  <FileSpreadsheet className="mr-2 h-3 w-3" /> Excel
                </Button>
                <Button size="sm" variant="outline" onClick={() => doExport(entity, "pdf")}>
                  <FileText className="mr-2 h-3 w-3" /> PDF
                </Button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
