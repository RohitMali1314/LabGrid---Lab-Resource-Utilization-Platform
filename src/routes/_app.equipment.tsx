import { createFileRoute } from "@tanstack/react-router";
import { useMemo, useState } from "react";
import { toast } from "sonner";
import { Search } from "lucide-react";
import { PageHeader } from "@/components/page-header";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { SearchableSelect } from "@/components/searchable-select";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { EmptyState, ErrorState, LoadingState } from "@/components/async-state";
import { EquipmentImage } from "@/components/equipment-image";
import { QuickBookingDialog } from "@/components/booking-dialog";
import { useApi } from "@/hooks/use-api";
import { listEquipment, deleteEquipment, type Equipment } from "@/services/equipmentService";
import {
  listDepartments, listEquipmentCategories, departmentMap, categoryMap,
  type Department, type EquipmentCategory,
} from "@/services/referenceService";
import { apiErrorMessage } from "@/services/api";
import { useAuth } from "@/lib/auth";
import { can } from "@/lib/permissions";


export const Route = createFileRoute("/_app/equipment")({
  component: EquipmentPage,
  head: () => ({
    meta: [
      { title: "Equipment Management · LabGrid" },
      {
        name: "description",
        content: "Browse, filter and manage lab equipment by department, category and status.",
      },
      { property: "og:title", content: "Equipment Management · LabGrid" },
      {
        property: "og:description",
        content: "Browse, filter and manage lab equipment by department, category and status.",
      },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
});

function EquipmentPage() {
  const { user } = useAuth();
  const canManage = can(user?.role, "equipment.manage");
  const canBook = can(user?.role, "bookings.create");
  const [bookTarget, setBookTarget] = useState<Equipment | null>(null);
  const state = useApi<Equipment[]>(listEquipment, []);

  const departments = useApi<Department[]>(listDepartments, []);
  const categories = useApi<EquipmentCategory[]>(listEquipmentCategories, []);
  const [q, setQ] = useState("");
  const [status, setStatus] = useState("all");
  const [departmentId, setDepartmentId] = useState("all");
  const [categoryId, setCategoryId] = useState("all");

  const deptNames = useMemo(() => departmentMap(departments.data), [departments.data]);
  const catNames = useMemo(() => categoryMap(categories.data), [categories.data]);

  const filtered = useMemo(() => {
    const list = state.data ?? [];
    const term = q.toLowerCase();
    return list.filter((e) => {
      const okStatus = status === "all" || e.status === status;
      const okDept = departmentId === "all" || String(e.departmentId ?? "") === departmentId;
      const okCat = categoryId === "all" || String(e.categoryId ?? "") === categoryId;
      const hay = `${e.equipmentName} ${e.modelNo ?? ""} ${e.serialNo ?? ""} ${e.description ?? ""}`.toLowerCase();
      return okStatus && okDept && okCat && hay.includes(term);
    });
  }, [state.data, q, status, departmentId, categoryId]);

  const remove = async (e: Equipment) => {
    if (!confirm(`Delete ${e.equipmentName}?`)) return;
    try {
      await deleteEquipment(e.equipmentId);
      toast.success("Equipment deleted");
      state.reload();
    } catch (err) {
      toast.error(apiErrorMessage(err, "Failed to delete equipment"));
    }
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Equipment Management"
        description="Browse, filter, and manage lab equipment."
      />
      <div className="flex flex-wrap gap-3">
        <div className="relative flex-1 min-w-[220px]">
          <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input placeholder="Search equipment..." value={q} onChange={(e) => setQ(e.target.value)} className="pl-9" />
        </div>
        <div className="w-full sm:w-56">
          <SearchableSelect
            value={departmentId}
            onChange={setDepartmentId}
            placeholder={departments.loading ? "Loading departments…" : "All departments"}
            searchPlaceholder="Search departments..."
            options={[
              { value: "all", label: "All departments" },
              ...(departments.data ?? []).map((d) => ({
                value: String(d.departmentId),
                label: d.departmentName,
              })),
            ]}
          />
        </div>
        <div className="w-full sm:w-56">
          <SearchableSelect
            value={categoryId}
            onChange={setCategoryId}
            placeholder={categories.loading ? "Loading categories…" : "All categories"}
            searchPlaceholder="Search categories..."
            options={[
              { value: "all", label: "All categories" },
              ...(categories.data ?? []).map((c) => ({
                value: String(c.categoryId),
                label: c.categoryName,
              })),
            ]}
          />
        </div>
        <Select value={status} onValueChange={setStatus}>
          <SelectTrigger className="w-full sm:w-48"><SelectValue /></SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All statuses</SelectItem>
            <SelectItem value="AVAILABLE">Available</SelectItem>
            <SelectItem value="BOOKED">Booked</SelectItem>
            <SelectItem value="UNDER_MAINTENANCE">Under maintenance</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {state.loading ? <LoadingState label="Loading equipment..." /> : state.error ? <ErrorState message={state.error} kind={state.errorKind} onRetry={state.reload} /> :
        filtered.length === 0 ? <EmptyState title="No equipment found" description="Try adjusting your filters." /> : (
        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {filtered.map((e) => (
            <article key={e.id} className="flex flex-col overflow-hidden rounded-xl border bg-card">
              <EquipmentImage
                variant="cover"
                className="rounded-none border-0 border-b"
                equipmentId={e.equipmentId}
                equipmentName={e.equipmentName}
                serialNo={e.serialNo}
              />
              <div className="flex flex-1 flex-col gap-3 p-4">
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0">
                    <h3 className="truncate font-semibold">{e.equipmentName}</h3>
                    <p className="mt-0.5 truncate text-xs text-muted-foreground">
                      {[e.modelNo && `Model ${e.modelNo}`, e.serialNo && `SN ${e.serialNo}`, `#${e.equipmentId}`]
                        .filter(Boolean)
                        .join(" · ")}
                    </p>
                  </div>
                  {e.status && (
                    <Badge variant={e.status === "AVAILABLE" ? "default" : e.status === "UNDER_MAINTENANCE" ? "destructive" : "secondary"}>
                      {e.status.replace(/_/g, " ")}
                    </Badge>
                  )}
                </div>

                <p className="line-clamp-2 min-h-[2.5rem] text-sm text-muted-foreground">
                  {e.description || "—"}
                </p>

                <dl className="grid grid-cols-2 gap-1 text-xs text-muted-foreground">
                  <div className="truncate">
                    <dt className="inline font-medium text-foreground">Dept: </dt>
                    <dd className="inline">{e.departmentId ? (deptNames.get(e.departmentId) ?? `#${e.departmentId}`) : "—"}</dd>
                  </div>
                  <div className="truncate">
                    <dt className="inline font-medium text-foreground">Category: </dt>
                    <dd className="inline">{e.categoryId ? (catNames.get(e.categoryId) ?? `#${e.categoryId}`) : "—"}</dd>
                  </div>
                </dl>

                <div className="mt-auto flex gap-2 pt-1">
                  {canBook && (
                    <Button variant="outline" className="flex-1" onClick={() => setBookTarget(e)}>
                      Book
                    </Button>
                  )}
                  {canManage && (
                    <Button variant="ghost" onClick={() => remove(e)}>Delete</Button>
                  )}
                </div>

              </div>
            </article>
          ))}
        </div>
      )}

      <QuickBookingDialog
        equipment={bookTarget}
        departmentName={
          bookTarget?.departmentId ? deptNames.get(bookTarget.departmentId) : undefined
        }
        open={bookTarget !== null}
        onOpenChange={(v) => !v && setBookTarget(null)}
        onBooked={state.reload}
      />

    </div>
  );
}
