import { useEffect, useState } from "react";
import { cn } from "@/lib/utils";
import { EQUIPMENT_PLACEHOLDER, equipmentImageSrc } from "@/lib/equipment-images";

interface Props {
  equipmentId?: number;
  equipmentName?: string;
  /** Serial number is the primary key used to resolve the photo. */
  serialNo?: string;
  className?: string;
  /** Small square thumbnail (tables/lists) vs. full-width card image. */
  variant?: "thumb" | "cover";
}

/** Equipment photo with a graceful placeholder fallback (never a broken image). */
export function EquipmentImage({
  equipmentId,
  equipmentName,
  serialNo,
  className,
  variant = "thumb",
}: Props) {
  const resolved = equipmentImageSrc(equipmentId, equipmentName, serialNo);
  const [src, setSrc] = useState(resolved);
  useEffect(() => setSrc(resolved), [resolved]);

  return (
    <img
      src={src}
      alt={equipmentName ? `${equipmentName} equipment photo` : "Lab equipment photo"}
      loading="lazy"
      decoding="async"
      onError={() => setSrc(EQUIPMENT_PLACEHOLDER)}
      className={cn(
        "bg-muted object-cover",
        variant === "thumb"
          ? "h-12 w-12 shrink-0 rounded-md border"
          : "aspect-[16/10] w-full rounded-lg border",
        className,
      )}
    />
  );
}
