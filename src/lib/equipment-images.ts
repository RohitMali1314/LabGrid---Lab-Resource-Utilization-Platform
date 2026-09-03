/**
 * Frontend-only equipment imagery.
 * Photos live in `public/equipment/` and are named after the equipment's
 * SERIAL NUMBER (the stable, unique key in the backend `equipment` table),
 * e.g. `/equipment/CHEM001.jpg`. No backend/database change is required.
 *
 * Resolution order:
 *   1. explicit equipmentId override (rare)
 *   2. serial number (with a few filename aliases, e.g. CYB001 -> CYBER001)
 *   3. name/model slug for legacy records
 *   4. generic placeholder
 */

/** equipmentId -> image path under /equipment/. Optional per-record override. */
export const equipmentImages: Record<number, string> = {};

export const EQUIPMENT_PLACEHOLDER = "/equipment/placeholder.jpg";

/** Serial numbers that have a photo in public/equipment/<serial>.jpg. */
const SERIAL_FILES = new Set([
  // Chemistry
  "CHEM001", "CHEM002", "CHEM003", "CHEM004", "CHEM005",
  "CHEM006", "CHEM007", "CHEM008", "CHEM009", "CHEM010",
  // Computer / IT
  "COMP001", "COMP002", "COMP003", "COMP004", "COMP005",
  "COMP006", "COMP007", "COMP008", "COMP009", "COMP010",
  // Cyber security
  "CYBER001", "CYBER002", "CYBER003", "CYBER004", "CYBER005",
  "CYBER006", "CYBER007", "CYBER008", "CYBER009", "CYBER010",
  // Electronics / electrical
  "ELEC004", "ELEC005", "ELEC006", "ELEC007",
  "ELEC008", "ELEC009", "ELEC010",
  "DM123456", "OSC123456", "OSC999",
  // Mechanical
  "MECH001", "MECH002", "MECH003", "MECH004", "MECH005",
  "MECH006", "MECH007", "MECH008", "MECH009", "MECH010",
  // Robotics / embedded
  "ROBO001", "ROBO002", "ROBO003", "ROBO004", "ROBO005",
  "ROBO006", "ROBO007", "ROBO008", "ROBO009", "ROBO010",
]);

/** Database serial -> image filename, where the two differ. */
const SERIAL_ALIASES: Record<string, string> = {
  CYB001: "CYBER001", CYB002: "CYBER002", CYB003: "CYBER003", CYB004: "CYBER004", CYB005: "CYBER005",
  CYB006: "CYBER006", CYB007: "CYBER007", CYB008: "CYBER008", CYB009: "CYBER009", CYB010: "CYBER010",
  ROB001: "ROBO001", ROB002: "ROBO002", ROB003: "ROBO003", ROB004: "ROBO004", ROB005: "ROBO005",
  ROB006: "ROBO006", ROB007: "ROBO007", ROB008: "ROBO008", ROB009: "ROBO009", ROB010: "ROBO010",
  ELEC001: "OSC999", ELEC002: "OSC123456", ELEC003: "DM123456",
};

/** Legacy name-slug images kept for records without a mapped serial. */
const KNOWN_SLUGS = new Set([
  "digital-multimeter",
  "oscilloscope",
  "laptop",
  "server",
  "laser-printer",
  "soldering-station",
]);

export function slugifyEquipmentName(name?: string) {
  return (name ?? "")
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/(^-|-$)/g, "");
}

function bySerial(serialNo?: string): string | null {
  const raw = (serialNo ?? "").trim().toUpperCase();
  if (!raw) return null;
  const file = SERIAL_FILES.has(raw) ? raw : SERIAL_ALIASES[raw];
  return file ? `/equipment/${file}.jpg` : null;
}

/** Resolves the best available image for an equipment record. */
export function equipmentImageSrc(
  equipmentId?: number,
  equipmentName?: string,
  serialNo?: string,
): string {
  if (equipmentId != null && equipmentImages[equipmentId]) return equipmentImages[equipmentId];
  const serialHit = bySerial(serialNo);
  if (serialHit) return serialHit;
  const slug = slugifyEquipmentName(equipmentName);
  if (KNOWN_SLUGS.has(slug)) return `/equipment/${slug}.jpg`;
  for (const known of KNOWN_SLUGS) {
    if (slug.includes(known)) return `/equipment/${known}.jpg`;
  }
  return EQUIPMENT_PLACEHOLDER;
}
