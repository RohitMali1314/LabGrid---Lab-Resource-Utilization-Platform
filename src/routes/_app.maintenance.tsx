import { useMemo, useState } from "react";
import { createFileRoute } from "@tanstack/react-router";
import { toast } from "sonner";
import { PageHeader, StatCard } from "@/components/page-header";
import { can } from "@/lib/permissions";
import { DataTable, type DataTableColumn } from "@/components/data-table";
import { SearchableSelect } from "@/components/searchable-select";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Checkbox } from "@/components/ui/checkbox";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useApi } from "@/hooks/use-api";
import { useAuth } from "@/lib/auth";
import {
  completeMaintenance,
  createMaintenance,
  getMaintenanceCounters,
  listMaintenance,
  listPendingCalibrations,
  startMaintenance,
  MAINTENANCE_PRIORITIES,
  MAINTENANCE_TYPES,
  type CreateMaintenancePayload,
  type MaintenanceCounters,
  type MaintenanceRequest,
} from "@/services/maintenanceService";
import { listEquipment, type Equipment } from "@/services/equipmentService";
import { listUsers, type AppUser } from "@/services/userService";
import { apiErrorMessage } from "@/services/api";
import { Activity, CheckCircle2, Play, Plus, Wrench } from "lucide-react";

export const Route = createFileRoute("/_app/maintenance")({
  component: MaintenancePage,
  head: () => ({
    meta: [
      { title: "Maintenance Management | LabGrid" },
      { name: "description", content: "Track lab equipment maintenance, calibration schedules and technician work orders." },
      { property: "og:title", content: "Maintenance Management | LabGrid" },
      { property: "og:description", content: "Track lab equipment maintenance, calibration and work orders." },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary" },
    ],
  }),
});

const STATUS_VARIANT: Record<string, "default" | "secondary" | "outline" | "destructive"> = {
  PENDING: "secondary",
  IN_PROGRESS: "default",
  COMPLETED: "outline",
};

const PRIORITY_VARIANT: Record<string, "default" | "secondary" | "outline" | "destructive"> = {
  LOW: "outline",
  MEDIUM: "secondary",
  HIGH: "default",
  CRITICAL: "destructive",
};

const fmt = (d?: string | null) => (d ? new Date(d).toLocaleString() : "—");
const fmtDay = (d?: string | null) => (d ? new Date(d).toLocaleDateString() : "—");
const toIso = (v: string) => (v ? (v.length === 16 ? `${v}:00` : v) : null);

const emptyForm = {
  equipmentId: "",
  technicianId: "",
  maintenanceType: "PREVENTIVE",
  priority: "MEDIUM",
  issue: "",
  issueDescription: "",
  scheduledDate: "",
  estimatedCompletionDate: "",
  calibrationRequired: false,
  calibrationDate: "",
  nextCalibrationDate: "",
  certificateNumber: "",
  remarks: "",
};

function MaintenancePage() {
  const { user } = useAuth();
  const records = useApi<MaintenanceRequest[]>(listMaintenance, []);
  const counters = useApi<MaintenanceCounters>(getMaintenanceCounters, []);
  // Calibration data is LAB_MANAGER / LAB_TECHNICIAN only — never requested otherwise.
  const canCalibrate = can(user?.role, "maintenance.calibration");
  const canManage = can(user?.role, "maintenance.manage");
  const canListUsers = can(user?.role, "users.view");
  const calibrations = useApi<MaintenanceRequest[]>(listPendingCalibrations, [canCalibrate], {
    enabled: canCalibrate,
  });
  const equipment = useApi<Equipment[]>(listEquipment, []);
  const users = useApi<AppUser[]>(listUsers, [canListUsers], { enabled: canListUsers });

  const [open, setOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [statusFilter, setStatusFilter] = useState("ALL");

  const equipmentName = useMemo(() => {
    const m = new Map<number, string>();
    (equipment.data ?? []).forEach((e) => m.set(e.equipmentId, e.equipmentName));
    return m;
  }, [equipment.data]);

  const userName = useMemo(() => {
    const m = new Map<number, string>();
    (users.data ?? []).forEach((u) => m.set(u.userId, u.name || u.email));
    return m;
  }, [users.data]);

  const technicianOptions = (users.data ?? [])
    .filter((u) => u.roleName === "LAB_TECHNICIAN")
    .map((u) => ({ value: String(u.userId), label: u.name || u.email, hint: u.email }));

  const equipmentOptions = (equipment.data ?? []).map((e) => ({
    value: String(e.equipmentId),
    label: e.equipmentName,
    hint: e.serialNo ? `SN ${e.serialNo}` : undefined,
  }));

  const rows = (records.data ?? []).filter(
    (r) => statusFilter === "ALL" || r.status === statusFilter,
  );

  const refreshAll = () => {
    void records.reload();
    void counters.reload();
    if (canCalibrate) void calibrations.reload();
  };

  const act = async (fn: () => Promise<unknown>, ok: string) => {
    try {
      await fn();
      toast.success(ok);
      refreshAll();
    } catch (err) {
      toast.error(apiErrorMessage(err, "Action failed"));
    }
  };

  const submit = async () => {
    if (!form.equipmentId) {
      toast.error("Select the equipment being serviced.");
      return;
    }
    if (!form.issue.trim()) {
      toast.error("Describe the issue.");
      return;
    }
    setSaving(true);
    const payload: CreateMaintenancePayload = {
      equipmentId: Number(form.equipmentId),
      technicianId: form.technicianId ? Number(form.technicianId) : null,
      reportedBy: user?.id ?? null,
      issue: form.issue.trim(),
      issueDescription: form.issueDescription.trim() || undefined,
      maintenanceType: form.maintenanceType,
      priority: form.priority,
      scheduledDate: toIso(form.scheduledDate),
      estimatedCompletionDate: toIso(form.estimatedCompletionDate),
      calibrationRequired: form.calibrationRequired,
      calibrationDate: form.calibrationRequired ? toIso(form.calibrationDate) : null,
      nextCalibrationDate: form.calibrationRequired ? toIso(form.nextCalibrationDate) : null,
      certificateNumber: form.calibrationRequired ? form.certificateNumber.trim() || null : null,
      remarks: form.remarks.trim() || null,
    };
    try {
      await createMaintenance(payload);
      toast.success("Maintenance request created");
      setForm(emptyForm);
      setOpen(false);
      refreshAll();
    } catch (err) {
      toast.error(apiErrorMessage(err, "Could not create the request"));
    } finally {
      setSaving(false);
    }
  };

  const columns: DataTableColumn<MaintenanceRequest>[] = [
    {
      key: "workOrder",
      header: "Work Order",
      value: (r) => r.workOrderNumber ?? `MR-${r.id}`,
      cell: (r) => <span className="font-mono text-xs">{r.workOrderNumber ?? `MR-${r.id}`}</span>,
    },
    {
      key: "equipment",
      header: "Equipment",
      value: (r) => equipmentName.get(r.equipmentId) ?? `#${r.equipmentId}`,
      cell: (r) => <span className="font-medium">{equipmentName.get(r.equipmentId) ?? `#${r.equipmentId}`}</span>,
    },
    { key: "technician", header: "Technician", value: (r) => (r.technicianId ? userName.get(r.technicianId) ?? `#${r.technicianId}` : "Unassigned") },
    { key: "reportedBy", header: "Reported By", value: (r) => (r.reportedBy ? userName.get(r.reportedBy) ?? `#${r.reportedBy}` : "—") },
    { key: "issue", header: "Issue", value: (r) => r.issue ?? r.issueDescription ?? "—" },
    { key: "type", header: "Type", value: (r) => r.maintenanceType ?? "—", cell: (r) => <Badge variant="secondary">{r.maintenanceType ?? "—"}</Badge> },
    {
      key: "priority",
      header: "Priority",
      value: (r) => r.priority ?? "",
      cell: (r) => <Badge variant={PRIORITY_VARIANT[r.priority ?? ""] ?? "outline"}>{r.priority ?? "—"}</Badge>,
    },
    {
      key: "status",
      header: "Status",
      value: (r) => r.status,
      cell: (r) => <Badge variant={STATUS_VARIANT[r.status] ?? "outline"}>{r.status.replace("_", " ")}</Badge>,
    },
    { key: "scheduled", header: "Scheduled", value: (r) => r.scheduledDate ?? "", cell: (r) => <span className="text-xs">{fmt(r.scheduledDate)}</span> },
    { key: "eta", header: "Est. Completion", value: (r) => r.estimatedCompletionDate ?? "", cell: (r) => <span className="text-xs">{fmt(r.estimatedCompletionDate)}</span> },
    ...(canCalibrate ? [{
      key: "calibration",
      header: "Calibration",
      value: (r) => (r.calibrationRequired ? r.nextCalibrationDate ?? "required" : ""),
      cell: (r) =>
        r.calibrationRequired ? (
          <div className="text-xs">
            <div>Done: {fmtDay(r.calibrationDate)}</div>
            <div>Next: {fmtDay(r.nextCalibrationDate)}</div>
            {r.certificateNumber && <div className="font-mono">{r.certificateNumber}</div>}
          </div>
        ) : (
          <span className="text-xs text-muted-foreground">Not required</span>
        ),
    } as DataTableColumn<MaintenanceRequest>] : []),
    // Start / Complete are LAB_MANAGER + LAB_TECHNICIAN only on the backend.
    ...(canManage ? [{
      key: "actions",
      header: "Actions",
      sortable: false,
      headerClassName: "text-right",
      className: "text-right",
      cell: (r) => (
        <div className="flex justify-end gap-1">
          {r.status === "PENDING" && (
            <Button size="sm" variant="outline" onClick={() => act(() => startMaintenance(r.id), "Maintenance started")}>
              <Play className="mr-1 h-3 w-3" /> Start
            </Button>
          )}
          {r.status === "IN_PROGRESS" && (
            <Button
              size="sm"
              onClick={() => {
                const remarks = window.prompt("Completion remarks (optional)") ?? undefined;
                void act(() => completeMaintenance(r.id, remarks || undefined), "Maintenance completed");
              }}
            >
              <CheckCircle2 className="mr-1 h-3 w-3" /> Complete
            </Button>
          )}
          {r.status === "COMPLETED" && (
            <span className="text-xs text-muted-foreground">
              {r.downtimeHours ? `${r.downtimeHours}h downtime` : fmtDay(r.completedDate)}
            </span>
          )}
        </div>
      ),
    } as DataTableColumn<MaintenanceRequest>] : []),
  ];

  const c = counters.data;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Maintenance"
        description="Work orders, calibration schedules and technician assignments."
        actions={
          canManage ? (
          <Dialog open={open} onOpenChange={setOpen}>

            <DialogTrigger asChild>
              <Button size="sm"><Plus className="mr-2 h-3 w-3" /> New request</Button>
            </DialogTrigger>
            <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-lg">
              <DialogHeader>
                <DialogTitle>New maintenance request</DialogTitle>
                <DialogDescription>Logged against your account as the reporter.</DialogDescription>
              </DialogHeader>
              <div className="grid gap-4">
                <div className="grid gap-2">
                  <Label>Equipment</Label>
                  <SearchableSelect
                    options={equipmentOptions}
                    value={form.equipmentId}
                    onChange={(v) => setForm((f) => ({ ...f, equipmentId: v }))}
                    placeholder="Select equipment"
                  />
                </div>
                <div className="grid gap-2">
                  <Label>Technician (optional)</Label>
                  <SearchableSelect
                    options={technicianOptions}
                    value={form.technicianId}
                    onChange={(v) => setForm((f) => ({ ...f, technicianId: v }))}
                    placeholder="Assign later"
                  />
                </div>
                <div className="grid gap-4 sm:grid-cols-2">
                  <div className="grid gap-2">
                    <Label>Type</Label>
                    <Select value={form.maintenanceType} onValueChange={(v) => setForm((f) => ({ ...f, maintenanceType: v }))}>
                      <SelectTrigger><SelectValue /></SelectTrigger>
                      <SelectContent>
                        {MAINTENANCE_TYPES.map((t) => <SelectItem key={t} value={t}>{t}</SelectItem>)}
                      </SelectContent>
                    </Select>
                  </div>
                  <div className="grid gap-2">
                    <Label>Priority</Label>
                    <Select value={form.priority} onValueChange={(v) => setForm((f) => ({ ...f, priority: v }))}>
                      <SelectTrigger><SelectValue /></SelectTrigger>
                      <SelectContent>
                        {MAINTENANCE_PRIORITIES.map((p) => <SelectItem key={p} value={p}>{p}</SelectItem>)}
                      </SelectContent>
                    </Select>
                  </div>
                </div>
                <div className="grid gap-2">
                  <Label htmlFor="issue">Issue</Label>
                  <Input id="issue" value={form.issue} onChange={(e) => setForm((f) => ({ ...f, issue: e.target.value }))} placeholder="Short summary" />
                </div>
                <div className="grid gap-2">
                  <Label htmlFor="issue-desc">Description</Label>
                  <Textarea id="issue-desc" value={form.issueDescription} onChange={(e) => setForm((f) => ({ ...f, issueDescription: e.target.value }))} rows={3} />
                </div>
                <div className="grid gap-4 sm:grid-cols-2">
                  <div className="grid gap-2">
                    <Label htmlFor="sched">Scheduled date</Label>
                    <Input id="sched" type="datetime-local" value={form.scheduledDate} onChange={(e) => setForm((f) => ({ ...f, scheduledDate: e.target.value }))} />
                  </div>
                  <div className="grid gap-2">
                    <Label htmlFor="eta">Estimated completion</Label>
                    <Input id="eta" type="datetime-local" value={form.estimatedCompletionDate} onChange={(e) => setForm((f) => ({ ...f, estimatedCompletionDate: e.target.value }))} />
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <Checkbox
                    id="calib"
                    checked={form.calibrationRequired}
                    onCheckedChange={(v) => setForm((f) => ({ ...f, calibrationRequired: v === true }))}
                  />
                  <Label htmlFor="calib">Calibration required</Label>
                </div>
                {form.calibrationRequired && (
                  <div className="grid gap-4 rounded-lg border p-3 sm:grid-cols-2">
                    <div className="grid gap-2">
                      <Label htmlFor="cal-date">Calibration date</Label>
                      <Input id="cal-date" type="datetime-local" value={form.calibrationDate} onChange={(e) => setForm((f) => ({ ...f, calibrationDate: e.target.value }))} />
                    </div>
                    <div className="grid gap-2">
                      <Label htmlFor="next-cal">Next calibration</Label>
                      <Input id="next-cal" type="datetime-local" value={form.nextCalibrationDate} onChange={(e) => setForm((f) => ({ ...f, nextCalibrationDate: e.target.value }))} />
                    </div>
                    <div className="grid gap-2 sm:col-span-2">
                      <Label htmlFor="cert">Certificate number</Label>
                      <Input id="cert" value={form.certificateNumber} onChange={(e) => setForm((f) => ({ ...f, certificateNumber: e.target.value }))} />
                    </div>
                  </div>
                )}
                <div className="grid gap-2">
                  <Label htmlFor="remarks">Remarks</Label>
                  <Textarea id="remarks" value={form.remarks} onChange={(e) => setForm((f) => ({ ...f, remarks: e.target.value }))} rows={2} />
                </div>
              </div>
              <DialogFooter>
                <Button variant="outline" onClick={() => setOpen(false)}>Cancel</Button>
                <Button onClick={submit} disabled={saving}>{saving ? "Saving…" : "Create request"}</Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
          ) : null
        }

      />

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard label="Total" value={counters.loading ? "…" : (c?.total ?? 0)} icon={<Wrench className="h-4 w-4" />} />
        <StatCard label="Pending" value={counters.loading ? "…" : (c?.pending ?? 0)} />
        <StatCard label="In Progress" value={counters.loading ? "…" : (c?.inProgress ?? 0)} icon={<Activity className="h-4 w-4" />} />
        <StatCard label="Completed" value={counters.loading ? "…" : (c?.completed ?? 0)} icon={<CheckCircle2 className="h-4 w-4" />} />
        <StatCard label="Preventive" value={counters.loading ? "…" : (c?.preventive ?? 0)} />
        <StatCard label="Corrective" value={counters.loading ? "…" : (c?.corrective ?? 0)} />
        {canCalibrate && (
          <>
            <StatCard label="Calibration" value={counters.loading ? "…" : (c?.calibration ?? 0)} />
            <StatCard
              label="Calibrations Due"
              value={calibrations.loading ? "…" : (calibrations.data?.length ?? 0)}
              hint="Next calibration date reached"
            />
          </>
        )}
      </div>

      <DataTable
        columns={columns}
        rows={rows}
        rowKey={(r) => r.id}
        loading={records.loading}
        error={records.error}
        onRetry={records.reload}
        searchPlaceholder="Search work orders, equipment, issues…"
        emptyTitle="No maintenance records"
        emptyDescription="Create a request to start tracking work orders."
        toolbar={
          <Select value={statusFilter} onValueChange={setStatusFilter}>
            <SelectTrigger className="h-9 w-44"><SelectValue /></SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">All statuses</SelectItem>
              <SelectItem value="PENDING">Pending</SelectItem>
              <SelectItem value="IN_PROGRESS">In progress</SelectItem>
              <SelectItem value="COMPLETED">Completed</SelectItem>
            </SelectContent>
          </Select>
        }
      />

      {canCalibrate && (
      <div>
        <h2 className="mb-3 text-sm font-semibold">Pending calibrations</h2>
        <DataTable
          columns={[
            { key: "wo", header: "Work Order", value: (r: MaintenanceRequest) => r.workOrderNumber ?? `MR-${r.id}` },
            { key: "eq", header: "Equipment", value: (r: MaintenanceRequest) => equipmentName.get(r.equipmentId) ?? `#${r.equipmentId}` },
            { key: "last", header: "Last Calibration", value: (r: MaintenanceRequest) => r.calibrationDate ?? "", cell: (r: MaintenanceRequest) => fmtDay(r.calibrationDate) },
            { key: "next", header: "Next Calibration", value: (r: MaintenanceRequest) => r.nextCalibrationDate ?? "", cell: (r: MaintenanceRequest) => fmtDay(r.nextCalibrationDate) },
            { key: "cert", header: "Certificate", value: (r: MaintenanceRequest) => r.certificateNumber ?? "—" },
          ]}
          rows={calibrations.data ?? []}
          rowKey={(r) => r.id}
          loading={calibrations.loading}
          error={calibrations.error}
          onRetry={calibrations.reload}
          pageSize={5}
          searchPlaceholder="Search calibrations…"
          emptyTitle="No calibrations due"
        />
      </div>
      )}
    </div>
  );
}
