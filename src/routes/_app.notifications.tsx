import { createFileRoute } from "@tanstack/react-router";
import { toast } from "sonner";
import { PageHeader, StatCard } from "@/components/page-header";
import { DataTable, type DataTableColumn } from "@/components/data-table";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { useNotifications } from "@/lib/notifications";
import { apiErrorMessage } from "@/services/api";
import type { Notification } from "@/services/notificationService";
import { Bell, CheckCheck, Trash2 } from "lucide-react";

export const Route = createFileRoute("/_app/notifications")({
  component: NotificationsPage,
  head: () => ({
    meta: [
      { title: "Notifications Center | LabGrid" },
      { name: "description", content: "Real-time booking, maintenance and equipment alerts for your lab account." },
      { property: "og:title", content: "Notifications Center | LabGrid" },
      { property: "og:description", content: "Real-time lab booking and maintenance alerts." },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary" },
    ],
  }),
});

function NotificationsPage() {
  const { mine, all, unread, unreadCount, loading, error, canSeeAll, refresh, markRead, markAllRead, remove } =
    useNotifications();

  const run = async (fn: () => Promise<void>, ok: string) => {
    try {
      await fn();
      toast.success(ok);
    } catch (err) {
      toast.error(apiErrorMessage(err, "Action failed"));
    }
  };

  const rows = canSeeAll ? all : mine;

  const columns: DataTableColumn<Notification>[] = [
    {
      key: "title",
      header: "Notification",
      value: (n) => n.title,
      cell: (n) => (
        <div className="min-w-0">
          <p className={n.read ? "text-sm" : "text-sm font-semibold"}>{n.title}</p>
          {n.message && <p className="text-xs text-muted-foreground">{n.message}</p>}
        </div>
      ),
    },
    { key: "type", header: "Type", value: (n) => n.type ?? "", cell: (n) => <Badge variant="secondary">{n.type ?? "GENERAL"}</Badge> },
    { key: "ref", header: "Reference", value: (n) => n.referenceId ?? "", cell: (n) => <span className="font-mono text-xs">{n.referenceId ?? "—"}</span> },
    {
      key: "status",
      header: "Status",
      value: (n) => (n.read ? "Read" : "Unread"),
      cell: (n) => <Badge variant={n.read ? "outline" : "default"}>{n.read ? "Read" : "Unread"}</Badge>,
    },
    {
      key: "created",
      header: "Received",
      value: (n) => n.createdAt ?? "",
      cell: (n) => <span className="text-xs text-muted-foreground">{n.createdAt ? new Date(n.createdAt).toLocaleString() : "—"}</span>,
    },
    {
      key: "actions",
      header: "Actions",
      sortable: false,
      headerClassName: "text-right",
      className: "text-right",
      cell: (n) => (
        <div className="flex justify-end gap-1">
          {!n.read && (
            <Button size="sm" variant="outline" onClick={() => run(() => markRead(n.id), "Marked as read")}>
              <CheckCheck className="mr-1 h-3 w-3" /> Read
            </Button>
          )}
          {canSeeAll && (
            <Button size="sm" variant="ghost" aria-label="Delete" onClick={() => run(() => remove(n.id), "Notification deleted")}>
              <Trash2 className="h-3 w-3" />
            </Button>
          )}
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Notifications"
        description="Live alerts pushed over the real-time channel and stored history."
        actions={
          <>
            <Button size="sm" variant="outline" onClick={() => void refresh()}>Refresh</Button>
            <Button size="sm" disabled={unreadCount === 0} onClick={() => run(markAllRead, "All notifications marked read")}>
              <CheckCheck className="mr-2 h-3 w-3" /> Mark all read
            </Button>
          </>
        }
      />

      <div className="grid gap-4 sm:grid-cols-3">
        <StatCard label="Total" value={loading ? "…" : rows.length} icon={<Bell className="h-4 w-4" />} />
        <StatCard label="Unread" value={loading ? "…" : unreadCount} />
        <StatCard label="Read" value={loading ? "…" : Math.max(0, rows.length - unread.length)} />
      </div>

      <DataTable
        columns={columns}
        rows={rows}
        rowKey={(n) => n.id}
        loading={loading && rows.length === 0}
        error={error}
        onRetry={() => void refresh()}
        searchPlaceholder="Search notifications…"
        emptyTitle="No notifications yet"
        emptyDescription="Booking and maintenance events will appear here."
      />
    </div>
  );
}
