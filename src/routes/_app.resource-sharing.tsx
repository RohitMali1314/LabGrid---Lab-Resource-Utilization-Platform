import { createFileRoute } from "@tanstack/react-router";
import { useMemo, useState } from "react";
import { ArrowLeftRight, Building2, CheckCircle2, Clock, Handshake, XCircle } from "lucide-react";
import { toast } from "sonner";
import { PageHeader, StatCard } from "@/components/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { EmptyState, ErrorState, LoadingState, PermissionState } from "@/components/async-state";
import { useApi } from "@/hooks/use-api";
import {
  activateResourceShare, approveResourceShare, cancelResourceShare, createResourceShare,
  listResourceShares, rejectResourceShare,
  type ResourceShare, type ShareStatus,
} from "@/services/resourceSharingService";
import { apiErrorMessage } from "@/services/api";
import { useAuth } from "@/lib/auth";
import { can } from "@/lib/permissions";

export const Route = createFileRoute("/_app/resource-sharing")({
  component: SharingPage,
  head: () => ({
    meta: [
      { title: "Resource Sharing · LabGrid" },
      { name: "description", content: "Share equipment between partner institutions." },
      { property: "og:title", content: "Resource Sharing · LabGrid" },
      { property: "og:description", content: "Share equipment between partner institutions." },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
});

const statusVariant: Record<ShareStatus, "default" | "secondary" | "destructive" | "outline"> = {
  PENDING: "secondary",
  APPROVED: "default",
  ACTIVE: "default",
  REJECTED: "destructive",
  CANCELLED: "outline",
};

const fmtDate = (v?: string) => (v ? new Date(v).toLocaleDateString() : "—");

function CreateShareDialog({ onCreated }: { onCreated: () => void }) {
  const [open, setOpen] = useState(false);
  const [busy, setBusy] = useState(false);
  const [form, setForm] = useState({
    equipmentId: "", targetInstitutionId: "", startDate: "", endDate: "", sharingRate: "", remarks: "",
  });
  const set = (k: keyof typeof form) => (e: { target: { value: string } }) =>
    setForm((f) => ({ ...f, [k]: e.target.value }));

  const submit = async () => {
    if (!form.equipmentId || !form.targetInstitutionId || !form.startDate || !form.endDate) {
      toast.error("Equipment, target institution and the date range are required");
      return;
    }
    setBusy(true);
    try {
      await createResourceShare({
        equipmentId: Number(form.equipmentId),
        targetInstitutionId: Number(form.targetInstitutionId),
        startDate: form.startDate,
        endDate: form.endDate,
        sharingRate: form.sharingRate ? Number(form.sharingRate) : undefined,
        remarks: form.remarks || undefined,
      });
      toast.success("Sharing request created successfully.");
      setOpen(false);
      setForm({ equipmentId: "", targetInstitutionId: "", startDate: "", endDate: "", sharingRate: "", remarks: "" });
      onCreated();
    } catch (err) {
      toast.error(apiErrorMessage(err, "We couldn't create the sharing request."));
    } finally {
      setBusy(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={(v) => !busy && setOpen(v)}>
      <DialogTrigger asChild>
        <Button size="sm">
          <Handshake className="mr-2 h-3 w-3" /> Create share
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Create Resource Share</DialogTitle>
          <DialogDescription>Offer one of your equipment items to another institution.</DialogDescription>
        </DialogHeader>
        <div className="space-y-3">
          <div className="grid gap-3 sm:grid-cols-2">
            <div className="space-y-1">
              <Label htmlFor="rs-eq">Equipment ID</Label>
              <Input id="rs-eq" value={form.equipmentId} onChange={set("equipmentId")} />
            </div>
            <div className="space-y-1">
              <Label htmlFor="rs-inst">Target Institution ID</Label>
              <Input id="rs-inst" value={form.targetInstitutionId} onChange={set("targetInstitutionId")} />
            </div>
            <div className="space-y-1">
              <Label htmlFor="rs-start">Start date</Label>
              <Input id="rs-start" type="date" value={form.startDate} onChange={set("startDate")} />
            </div>
            <div className="space-y-1">
              <Label htmlFor="rs-end">End date</Label>
              <Input id="rs-end" type="date" value={form.endDate} onChange={set("endDate")} />
            </div>
          </div>
          <div className="space-y-1">
            <Label htmlFor="rs-rate">Sharing rate (per hour)</Label>
            <Input id="rs-rate" type="number" min="0" step="0.01" value={form.sharingRate} onChange={set("sharingRate")} />
          </div>
          <div className="space-y-1">
            <Label htmlFor="rs-remarks">Remarks</Label>
            <Textarea id="rs-remarks" rows={3} value={form.remarks} onChange={set("remarks")} />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => setOpen(false)} disabled={busy}>Cancel</Button>
          <Button onClick={submit} disabled={busy}>{busy ? "Creating…" : "Create share"}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function SharingPage() {
  const { user } = useAuth();
  const canView = can(user?.role, "resourceSharing.view");
  const canManage = can(user?.role, "resourceSharing.manage");
  const shares = useApi<ResourceShare[]>(listResourceShares, [], { enabled: canView });

  const rows = shares.data ?? [];
  const pending = useMemo(() => rows.filter((r) => r.status === "PENDING"), [rows]);
  const active = useMemo(() => rows.filter((r) => r.status === "ACTIVE"), [rows]);
  const partners = new Set(rows.map((r) => r.targetInstitutionId)).size;

  const act = async (label: string, fn: () => Promise<unknown>) => {
    try {
      await fn();
      toast.success(`${label} successfully.`);
      shares.reload();
    } catch (err) {
      toast.error(apiErrorMessage(err, `We couldn't complete this action.`));
    }
  };

  if (!canView) {
    return (
      <div className="space-y-6">
        <PageHeader title="Inter-Institution Sharing" description="Share equipment with partner institutions." />
        <PermissionState />
      </div>
    );
  }

  const ShareTable = ({ list, actions }: { list: ResourceShare[]; actions: boolean }) => (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Share</TableHead>
          <TableHead>Equipment</TableHead>
          <TableHead>Owner → Target</TableHead>
          <TableHead>Period</TableHead>
          <TableHead>Rate</TableHead>
          <TableHead>Status</TableHead>
          {actions && canManage && <TableHead className="text-right">Actions</TableHead>}
        </TableRow>
      </TableHeader>
      <TableBody>
        {list.map((r) => (
          <TableRow key={r.shareId}>
            <TableCell className="font-mono text-xs">#{r.shareId}</TableCell>
            <TableCell className="font-mono text-xs">#{r.equipmentId}</TableCell>
            <TableCell className="text-xs">
              #{r.ownerInstitutionId} → #{r.targetInstitutionId}
            </TableCell>
            <TableCell className="text-xs text-muted-foreground">
              {fmtDate(r.startDate)} – {fmtDate(r.endDate)}
            </TableCell>
            <TableCell className="text-xs">
              {r.sharingRate != null ? `${Number(r.sharingRate).toFixed(2)}/hr` : "—"}
            </TableCell>
            <TableCell>
              <Badge variant={statusVariant[r.status] ?? "secondary"}>{r.status}</Badge>
            </TableCell>
            {actions && canManage && (
              <TableCell className="space-x-2 text-right">
                {r.status === "PENDING" && (
                  <>
                    <Button size="sm" variant="outline"
                      onClick={() => act("Rejected", () => rejectResourceShare(r.shareId))}>
                      <XCircle className="mr-1 h-3 w-3" /> Reject
                    </Button>
                    <Button size="sm"
                      onClick={() => act("Approved", () => approveResourceShare(r.shareId))}>
                      <CheckCircle2 className="mr-1 h-3 w-3" /> Approve
                    </Button>
                  </>
                )}
                {r.status === "APPROVED" && (
                  <Button size="sm" onClick={() => act("Activated", () => activateResourceShare(r.shareId))}>
                    Activate
                  </Button>
                )}
                {(r.status === "APPROVED" || r.status === "ACTIVE" || r.status === "PENDING") && (
                  <Button size="sm" variant="ghost"
                    onClick={() => act("Cancelled", () => cancelResourceShare(r.shareId))}>
                    Cancel
                  </Button>
                )}
              </TableCell>
            )}
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );

  const Section = ({ list, emptyTitle, actions }: { list: ResourceShare[]; emptyTitle: string; actions: boolean }) =>
    shares.loading ? <LoadingState label="Loading resource sharing data..." />
    : shares.error ? <ErrorState message={shares.error} kind={shares.errorKind} onRetry={shares.reload} />
    : list.length === 0 ? <EmptyState title={emptyTitle} />
    : <ShareTable list={list} actions={actions} />;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Inter-Institution Sharing"
        description="Share specialised equipment with partner institutions."
        actions={canManage ? <CreateShareDialog onCreated={shares.reload} /> : undefined}
      />

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard label="Total Shares" value={rows.length} icon={<ArrowLeftRight className="h-4 w-4" />} />
        <StatCard label="Partner Institutions" value={partners} icon={<Building2 className="h-4 w-4" />} />
        <StatCard label="Pending Requests" value={pending.length} icon={<Clock className="h-4 w-4" />} />
        <StatCard label="Active Shares" value={active.length} icon={<CheckCircle2 className="h-4 w-4" />} />
      </div>

      <Tabs defaultValue="all" className="space-y-4">
        <TabsList>
          <TabsTrigger value="all">All Shares</TabsTrigger>
          <TabsTrigger value="pending">Pending</TabsTrigger>
          <TabsTrigger value="active">Active</TabsTrigger>
        </TabsList>
        <TabsContent value="all" className="rounded-xl border bg-card">
          <Section list={rows} emptyTitle="No resource sharing data available." actions />
        </TabsContent>
        <TabsContent value="pending" className="rounded-xl border bg-card">
          <Section list={pending} emptyTitle="No pending requests." actions />
        </TabsContent>
        <TabsContent value="active" className="rounded-xl border bg-card">
          <Section list={active} emptyTitle="No active shares." actions={false} />
        </TabsContent>
      </Tabs>
    </div>
  );
}
