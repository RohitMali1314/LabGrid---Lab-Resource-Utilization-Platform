import { createFileRoute } from "@tanstack/react-router";
import { useState } from "react";
import { toast } from "sonner";
import { Banknote, CheckCircle2, IndianRupee, Layers, XCircle } from "lucide-react";
import { PageHeader, StatCard } from "@/components/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { DataTable, type DataTableColumn } from "@/components/data-table";
import { AsyncBoundary, PermissionState } from "@/components/async-state";
import { useApi } from "@/hooks/use-api";
import { apiErrorMessage } from "@/services/api";
import { useAuth } from "@/lib/auth";
import { can } from "@/lib/permissions";
import {
  approveAllocation, cancelAllocation, cancelInvoice, getCostAnalysis, listAllocations,
  listBills, payAllocation, payInvoice,
  type Billing, type CostAllocation,
} from "@/services/billingService";

export const Route = createFileRoute("/_app/billing")({
  component: BillingPage,
  head: () => ({
    meta: [
      { title: "Billing & Cost Allocation · LabGrid" },
      { name: "description", content: "Institution invoices, shared-cost allocation and cost analysis." },
      { property: "og:title", content: "Billing & Cost Allocation · LabGrid" },
      { property: "og:description", content: "Institution invoices, shared-cost allocation and cost analysis." },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
});

const money = (v: number | null | undefined) =>
  v === null || v === undefined ? "—" : `₹${Number(v).toLocaleString("en-IN", { maximumFractionDigits: 2 })}`;

const dateOf = (v?: string | null) => (v ? new Date(v).toLocaleDateString() : "—");

function StatusBadge({ status }: { status?: string | null }) {
  const s = (status ?? "").toUpperCase();
  const variant =
    s === "PAID" || s === "APPROVED" ? "default"
      : s === "CANCELLED" || s === "REJECTED" ? "destructive"
        : "secondary";
  return <Badge variant={variant as never}>{s || "UNKNOWN"}</Badge>;
}

function BillingPage() {
  const { user } = useAuth();
  const allowed = can(user?.role, "billing.view");
  const canAnalyse = can(user?.role, "costAnalysis.view");
  // Mutations are INSTITUTION_ADMIN / SYSTEM_ADMIN only.
  const canManageBills = can(user?.role, "billing.manage");
  const canManageAllocations = can(user?.role, "costAllocation.manage");
  const [busy, setBusy] = useState<number | null>(null);

  const bills = useApi<Billing[]>(() => listBills(), [], { enabled: allowed });
  const allocations = useApi<CostAllocation[]>(() => listAllocations(), [], { enabled: allowed });
  const analysis = useApi(() => getCostAnalysis(), [], { enabled: canAnalyse });

  if (!allowed) {
    return (
      <div className="space-y-6">
        <PageHeader title="Billing & Cost Allocation" description="Inter-institution invoices and shared costs." />
        <PermissionState />
      </div>
    );
  }

  const act = async (id: number, fn: () => Promise<unknown>, ok: string, reload: () => void) => {
    setBusy(id);
    try {
      await fn();
      toast.success(ok);
      reload();
    } catch (err) {
      toast.error(apiErrorMessage(err, "We couldn't complete this action. Please try again."));
    } finally {
      setBusy(null);
    }
  };

  const billColumns: DataTableColumn<Billing>[] = [
    { key: "invoice", header: "Invoice", sortable: true, value: (b) => b.invoiceNumber ?? `#${b.billId}` },
    { key: "booking", header: "Booking", sortable: true, value: (b) => b.bookingId ?? "—" },
    { key: "institution", header: "Institution", sortable: true, value: (b) => b.institutionId ?? "—" },
    { key: "subtotal", header: "Subtotal", sortable: true, value: (b) => b.subtotal ?? 0, cell: (b) => money(b.subtotal) },
    { key: "gst", header: "GST", sortable: true, value: (b) => b.gst ?? 0, cell: (b) => money(b.gst) },
    { key: "total", header: "Grand total", sortable: true, value: (b) => b.grandTotal ?? 0, cell: (b) => money(b.grandTotal) },
    { key: "due", header: "Due", sortable: true, value: (b) => b.dueDate ?? "", cell: (b) => dateOf(b.dueDate) },
    { key: "status", header: "Status", sortable: true, value: (b) => b.status ?? "", cell: (b) => <StatusBadge status={b.status} /> },
    {
      key: "actions",
      header: "",
      cell: (b) => {
        const s = (b.status ?? "").toUpperCase();
        if (!canManageBills || s === "PAID" || s === "CANCELLED") return null;
        return (
          <div className="flex justify-end gap-2">
            <Button size="sm" disabled={busy === b.billId}
              onClick={() => act(b.billId, () => payInvoice(b.billId), "Invoice marked as paid.", bills.reload)}>
              Mark paid
            </Button>
            <Button size="sm" variant="outline" disabled={busy === b.billId}
              onClick={() => act(b.billId, () => cancelInvoice(b.billId), "Invoice cancelled.", bills.reload)}>
              Cancel
            </Button>
          </div>
        );
      },
      className: "text-right",
    },
  ];

  const allocationColumns: DataTableColumn<CostAllocation>[] = [
    { key: "id", header: "Allocation", sortable: true, value: (a) => a.allocationId },
    { key: "bill", header: "Bill", sortable: true, value: (a) => a.billId },
    { key: "booking", header: "Booking", sortable: true, value: (a) => a.bookingId ?? "—" },
    { key: "share", header: "Resource share", sortable: true, value: (a) => a.resourceShareId ?? "—" },
    { key: "institution", header: "Institution", sortable: true, value: (a) => a.institutionId },
    {
      key: "pct", header: "Share %", sortable: true, value: (a) => a.allocationPercentage ?? 0,
      cell: (a) => (a.allocationPercentage === null || a.allocationPercentage === undefined ? "—" : `${a.allocationPercentage}%`),
    },
    { key: "amount", header: "Amount", sortable: true, value: (a) => a.allocatedAmount ?? 0, cell: (a) => money(a.allocatedAmount) },
    { key: "status", header: "Status", sortable: true, value: (a) => a.status ?? "", cell: (a) => <StatusBadge status={a.status} /> },
    {
      key: "actions",
      header: "",
      className: "text-right",
      cell: (a) => {
        const s = (a.status ?? "").toUpperCase();
        if (!canManageAllocations || s === "PAID" || s === "CANCELLED") return null;
        return (
          <div className="flex justify-end gap-2">
            {s !== "APPROVED" && (
              <Button size="sm" variant="outline" disabled={busy === a.allocationId}
                onClick={() => act(a.allocationId, () => approveAllocation(a.allocationId), "Allocation approved.", allocations.reload)}>
                Approve
              </Button>
            )}
            <Button size="sm" disabled={busy === a.allocationId}
              onClick={() => act(a.allocationId, () => payAllocation(a.allocationId), "Allocation marked as paid.", allocations.reload)}>
              Mark paid
            </Button>
            <Button size="sm" variant="ghost" disabled={busy === a.allocationId}
              onClick={() => act(a.allocationId, () => cancelAllocation(a.allocationId), "Allocation cancelled.", allocations.reload)}>
              Cancel
            </Button>
          </div>
        );
      },
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Billing & Cost Allocation"
        description="Invoices, inter-institution cost sharing and consolidated cost analysis."
      />

      <Tabs defaultValue="invoices">
        <TabsList>
          <TabsTrigger value="invoices">Invoices</TabsTrigger>
          <TabsTrigger value="allocations">Cost Allocation</TabsTrigger>
          {canAnalyse && <TabsTrigger value="analysis">Cost Analysis</TabsTrigger>}
        </TabsList>

        <TabsContent value="invoices" className="mt-4">
          <DataTable
            columns={billColumns}
            rows={bills.data ?? []}
            rowKey={(b) => b.billId}
            loading={bills.loading}
            error={bills.error}
            onRetry={bills.reload}
            searchPlaceholder="Search invoices…"
            emptyTitle="No billing data available"
          />
        </TabsContent>

        <TabsContent value="allocations" className="mt-4">
          <DataTable
            columns={allocationColumns}
            rows={allocations.data ?? []}
            rowKey={(a) => a.allocationId}
            loading={allocations.loading}
            error={allocations.error}
            onRetry={allocations.reload}
            searchPlaceholder="Search allocations…"
            emptyTitle="No cost allocation data available"
          />
        </TabsContent>

        {canAnalyse && (
          <TabsContent value="analysis" className="mt-4">
            <AsyncBoundary
              state={analysis}
              emptyTitle="No cost analysis data available"
              emptyWhen={(d) => !d || (d.totalAllocations ?? 0) === 0}
            >
              {(d) => (
                <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                  <StatCard label="Total allocations" value={d.totalAllocations ?? 0} icon={<Layers className="h-4 w-4" />} />
                  <StatCard label="Allocated cost" value={money(d.totalAllocatedCost)} icon={<IndianRupee className="h-4 w-4" />} />
                  <StatCard label="Paid" value={money(d.paidCost)} icon={<CheckCircle2 className="h-4 w-4" />} />
                  <StatCard label="Approved" value={money(d.approvedCost)} icon={<Banknote className="h-4 w-4" />} />
                  <StatCard label="Pending" value={money(d.pendingCost)} icon={<Banknote className="h-4 w-4" />} />
                  <StatCard label="Cancelled" value={money(d.cancelledCost)} icon={<XCircle className="h-4 w-4" />} />
                </div>
              )}
            </AsyncBoundary>
          </TabsContent>
        )}
      </Tabs>
    </div>
  );
}
