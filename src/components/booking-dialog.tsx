import { useState } from "react";
import { toast } from "sonner";
import { Loader2 } from "lucide-react";
import {
  Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { EquipmentImage } from "@/components/equipment-image";
import { createBooking } from "@/services/bookingService";
import { apiErrorMessage, apiErrorKind } from "@/services/api";
import type { Equipment } from "@/services/equipmentService";

/** datetime-local value -> yyyy-MM-ddTHH:mm:ss expected by the backend. */
const money = (n: number) =>
  new Intl.NumberFormat(undefined, { style: "currency", currency: "INR", maximumFractionDigits: 2 }).format(n);

const toLocalDateTime = (v: string) => (v.length === 16 ? `${v}:00` : v);

/**
 * Booking modal with the equipment already pre-selected.
 * Uses the existing POST /api/bookings endpoint — nothing is created until the
 * user fills in real start/end/purpose values and confirms.
 */
export function QuickBookingDialog({
  equipment,
  departmentName,
  open,
  onOpenChange,
  onBooked,
}: {
  equipment: Equipment | null;
  departmentName?: string;
  open: boolean;
  onOpenChange: (v: boolean) => void;
  onBooked?: () => void;
}) {
  const [start, setStart] = useState("");
  const [end, setEnd] = useState("");
  const [purpose, setPurpose] = useState("");
  const [busy, setBusy] = useState(false);

  // Cost estimate derived from the real Equipment.hourlyRate returned by the API.
  const rate = Number(equipment?.hourlyRate ?? 0);
  const hours =
    start && end && new Date(end) > new Date(start)
      ? (new Date(end).getTime() - new Date(start).getTime()) / 3600000
      : 0;
  const estimate = rate > 0 && hours > 0 ? { rate, hours, total: rate * hours } : null;

  const close = (v: boolean) => {
    if (busy) return;
    if (!v) { setStart(""); setEnd(""); setPurpose(""); }
    onOpenChange(v);
  };

  const submit = async () => {
    if (!equipment) return;
    if (!start || !end) return toast.error("Please choose a start and end time");
    if (new Date(end) <= new Date(start)) return toast.error("End time must be after start time");
    setBusy(true);
    try {
      await createBooking({
        equipmentId: equipment.equipmentId,
        startTime: toLocalDateTime(start),
        endTime: toLocalDateTime(end),
        purpose: purpose || undefined,
      });
      toast.success("Booking request submitted successfully.");
      close(false);
      onBooked?.();
    } catch (err) {
      const kind = apiErrorKind(err);
      toast.error(
        kind === "conflict" || kind === "validation"
          ? apiErrorMessage(err, "This equipment is not currently available to your institution.")
          : apiErrorMessage(err, "We couldn't submit your booking. Please try again."),
      );
    } finally {
      setBusy(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={close}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Book Equipment</DialogTitle>
          <DialogDescription>Confirm your slot for the selected equipment.</DialogDescription>
        </DialogHeader>
        {equipment && (
          <div className="space-y-3">
            <div className="overflow-hidden rounded-xl border bg-card">
              <EquipmentImage
                variant="cover"
                className="rounded-none border-0 border-b"
                equipmentId={equipment.equipmentId}
                equipmentName={equipment.equipmentName}
                serialNo={equipment.serialNo}
              />
              <div className="p-3">
                <div className="text-sm font-medium">{equipment.equipmentName}</div>
                <div className="text-xs text-muted-foreground">
                  {departmentName ? `${departmentName} · ` : ""}
                  {equipment.modelNo ? `Model ${equipment.modelNo} · ` : ""}
                  {equipment.serialNo ? `SN ${equipment.serialNo} · ` : ""}#{equipment.equipmentId}
                </div>
              </div>
            </div>
            <div className="grid gap-3 sm:grid-cols-2">
              <div className="space-y-1">
                <Label htmlFor="qb-start">Start</Label>
                <Input id="qb-start" type="datetime-local" value={start}
                  onChange={(e) => setStart(e.target.value)} />
              </div>
              <div className="space-y-1">
                <Label htmlFor="qb-end">End</Label>
                <Input id="qb-end" type="datetime-local" value={end}
                  onChange={(e) => setEnd(e.target.value)} />
              </div>
            </div>
            {estimate && (
              <div className="flex items-center justify-between rounded-lg border bg-muted/40 px-3 py-2 text-xs">
                <span className="text-muted-foreground">
                  Estimated cost · {estimate.hours.toFixed(1)}h × {money(estimate.rate)}/h
                </span>
                <span className="font-semibold">{money(estimate.total)}</span>
              </div>
            )}
            <div className="space-y-1">
              <Label htmlFor="qb-purpose">Purpose</Label>
              <Textarea id="qb-purpose" rows={3} value={purpose}
                placeholder="Describe what you'll use this equipment for"
                onChange={(e) => setPurpose(e.target.value)} />
            </div>
          </div>
        )}
        <DialogFooter>
          <Button variant="outline" onClick={() => close(false)} disabled={busy}>Cancel</Button>
          <Button onClick={submit} disabled={busy || !equipment}>
            {busy && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            Confirm Booking
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
