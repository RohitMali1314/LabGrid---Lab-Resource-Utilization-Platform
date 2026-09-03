import { useState } from "react";
import { Bell, CheckCheck, Inbox, RefreshCw, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { LoadingState, ErrorState } from "@/components/async-state";
import { useNotifications } from "@/lib/notifications";
import { apiErrorMessage } from "@/services/api";
import type { Notification } from "@/services/notificationService";

function NotificationList({
  items,
  onRead,
  onDelete,
  canDelete,
}: {
  items: Notification[];
  onRead: (id: number) => void;
  onDelete: (id: number) => void;
  canDelete: boolean;
}) {
  if (items.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center gap-2 py-16 text-center">
        <Inbox className="h-8 w-8 text-muted-foreground" />
        <p className="text-sm font-medium">You're all caught up</p>
        <p className="max-w-xs text-xs text-muted-foreground">
          New alerts appear here instantly.
        </p>
      </div>
    );
  }
  return (
    <ul className="space-y-2">
      {items.map((n) => (
        <li
          key={n.id}
          className={`rounded-lg border p-3 transition ${n.read ? "bg-card" : "border-primary/40 bg-primary/5"}`}
        >
          <div className="flex items-start justify-between gap-2">
            <div className="min-w-0">
              <p className="truncate text-sm font-medium">{n.title}</p>
              {n.message && <p className="mt-1 text-xs text-muted-foreground">{n.message}</p>}
              {n.type && (
                <Badge variant="secondary" className="mt-2 h-4 px-1 text-[10px]">
                  {n.type}
                </Badge>
              )}
            </div>
            <div className="flex shrink-0 items-center gap-1">
              {!n.read && (
                <Button variant="ghost" size="icon" className="h-6 w-6" aria-label="Mark read" onClick={() => onRead(n.id)}>
                  <CheckCheck className="h-3 w-3" />
                </Button>
              )}
              {canDelete && (
                <Button variant="ghost" size="icon" className="h-6 w-6" aria-label="Delete" onClick={() => onDelete(n.id)}>
                  <Trash2 className="h-3 w-3" />
                </Button>
              )}
            </div>
          </div>
          {n.createdAt && (
            <p className="mt-2 text-[10px] text-muted-foreground">
              {new Date(n.createdAt).toLocaleString()}
            </p>
          )}
        </li>
      ))}
    </ul>
  );
}

export function NotificationsDrawer() {
  const { mine, all, unread, unreadCount, loading, error, canSeeAll, refresh, markRead, markAllRead, remove } =
    useNotifications();
  const [open, setOpen] = useState(false);

  const handle = (fn: () => Promise<void>, ok: string) => async () => {
    try {
      await fn();
      toast.success(ok);
    } catch (err) {
      toast.error(apiErrorMessage(err, "Action failed"));
    }
  };

  return (
    <Sheet open={open} onOpenChange={(o) => { setOpen(o); if (o) void refresh(); }}>
      <SheetTrigger asChild>
        <Button variant="ghost" size="icon" aria-label="Notifications" className="relative">
          <Bell className="h-4 w-4" />
          {unreadCount > 0 && (
            <Badge variant="destructive" className="absolute -right-1 -top-1 h-4 min-w-4 px-1 text-[10px]">
              {unreadCount}
            </Badge>
          )}
        </Button>
      </SheetTrigger>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader className="flex-row items-center justify-between space-y-0">
          <SheetTitle>Notifications</SheetTitle>
          <div className="flex items-center gap-1">
            <Button variant="ghost" size="icon" aria-label="Refresh" onClick={() => void refresh()}>
              <RefreshCw className="h-3 w-3" />
            </Button>
            {unreadCount > 0 && (
              <Button variant="ghost" size="sm" onClick={handle(markAllRead, "All notifications marked read")}>
                <CheckCheck className="mr-2 h-3 w-3" /> Mark all read
              </Button>
            )}
          </div>
        </SheetHeader>

        <Tabs defaultValue="mine" className="mt-4 flex flex-1 flex-col overflow-hidden">
          <TabsList className="w-full">
            <TabsTrigger value="mine" className="flex-1">Mine</TabsTrigger>
            <TabsTrigger value="unread" className="flex-1">Unread ({unreadCount})</TabsTrigger>
            {canSeeAll && <TabsTrigger value="all" className="flex-1">All</TabsTrigger>}
          </TabsList>

          <ScrollArea className="-mx-6 mt-3 flex-1 px-6">
            {loading && mine.length === 0 ? (
              <LoadingState />
            ) : error ? (
              <ErrorState message={error} onRetry={() => void refresh()} />
            ) : (
              <>
                <TabsContent value="mine">
                  <NotificationList
                    items={mine}
                    onRead={(id) => void handle(() => markRead(id), "Marked as read")()}
                    onDelete={(id) => void handle(() => remove(id), "Notification deleted")()}
                    canDelete={canSeeAll}
                  />
                </TabsContent>
                <TabsContent value="unread">
                  <NotificationList
                    items={unread}
                    onRead={(id) => void handle(() => markRead(id), "Marked as read")()}
                    onDelete={(id) => void handle(() => remove(id), "Notification deleted")()}
                    canDelete={canSeeAll}
                  />
                </TabsContent>
                {canSeeAll && (
                  <TabsContent value="all">
                    <NotificationList
                      items={all}
                      onRead={(id) => void handle(() => markRead(id), "Marked as read")()}
                      onDelete={(id) => void handle(() => remove(id), "Notification deleted")()}
                      canDelete
                    />
                  </TabsContent>
                )}
              </>
            )}
          </ScrollArea>
        </Tabs>
      </SheetContent>
    </Sheet>
  );
}
