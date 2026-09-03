import { Activity, CalendarCheck, CheckCircle2, Clock, FlaskConical, ListChecks, Loader2, PauseCircle, ShieldCheck, ThumbsUp, Wrench, XCircle } from "lucide-react";
import { StatCard } from "@/components/page-header";
import { ErrorState } from "@/components/async-state";
import { useApi } from "@/hooks/use-api";
import { getDashboardReport, type DashboardReport } from "@/services/reportService";

const GROUPS: { title: string; items: { key: keyof DashboardReport; label: string; icon: React.ReactNode }[] }[] = [
  {
    title: "Equipment",
    items: [
      { key: "totalEquipment", label: "Total Equipment", icon: <FlaskConical className="h-4 w-4" /> },
      { key: "availableEquipment", label: "Available", icon: <CheckCircle2 className="h-4 w-4" /> },
      { key: "bookedEquipment", label: "Booked", icon: <CalendarCheck className="h-4 w-4" /> },
      { key: "underMaintenanceEquipment", label: "Under Maintenance", icon: <Wrench className="h-4 w-4" /> },
    ],
  },
  {
    title: "Bookings",
    items: [
      { key: "totalBookings", label: "Total Bookings", icon: <ListChecks className="h-4 w-4" /> },
      { key: "pendingBookings", label: "Pending", icon: <Clock className="h-4 w-4" /> },
      { key: "approvedBookings", label: "Approved", icon: <ThumbsUp className="h-4 w-4" /> },
      { key: "completedBookings", label: "Completed", icon: <ShieldCheck className="h-4 w-4" /> },
    ],
  },
  {
    title: "Maintenance",
    items: [
      { key: "totalMaintenance", label: "Total Maintenance", icon: <Activity className="h-4 w-4" /> },
      { key: "pendingMaintenance", label: "Pending", icon: <PauseCircle className="h-4 w-4" /> },
      { key: "inProgressMaintenance", label: "In Progress", icon: <Loader2 className="h-4 w-4" /> },
      { key: "completedMaintenance", label: "Completed", icon: <XCircle className="h-4 w-4" /> },
    ],
  },
];

/** The 12 operational metrics from GET /api/reports/dashboard. */
export function DashboardStatsGrid() {
  const report = useApi<DashboardReport>(getDashboardReport, []);

  if (report.error) return <ErrorState message={report.error} onRetry={report.reload} />;

  return (
    <div className="space-y-5">
      {GROUPS.map((g) => (
        <section key={g.title} className="space-y-3">
          <h2 className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">{g.title}</h2>
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            {g.items.map((item) => (
              <StatCard
                key={item.key}
                label={item.label}
                value={report.loading ? "…" : (report.data?.[item.key] ?? 0)}
                icon={item.icon}
              />
            ))}
          </div>
        </section>
      ))}
    </div>
  );
}
