import { useEffect, useState } from "react";
import { listEquipment, type Equipment } from "@/services/equipmentService";
import { listDepartments, type Department } from "@/services/referenceService";

/**
 * Shared reference lookups (equipment + departments).
 *
 * Both lists are fetched ONCE per page session and cached at module scope, so
 * resolving `booking.equipmentId -> equipmentName` or
 * `equipment.departmentId -> departmentName` never causes N+1 requests.
 */

let equipmentPromise: Promise<Equipment[]> | null = null;
let departmentPromise: Promise<Department[]> | null = null;

export function loadEquipmentOnce(): Promise<Equipment[]> {
  if (!equipmentPromise) {
    equipmentPromise = listEquipment().catch((err) => {
      equipmentPromise = null;
      throw err;
    });
  }
  return equipmentPromise;
}

export function loadDepartmentsOnce(): Promise<Department[]> {
  if (!departmentPromise) {
    departmentPromise = listDepartments().catch((err) => {
      departmentPromise = null;
      throw err;
    });
  }
  return departmentPromise;
}

/** Drop the cache after a mutation (create / update / delete). */
export function invalidateLookups() {
  equipmentPromise = null;
  departmentPromise = null;
}

export interface Lookups {
  equipment: Equipment[];
  equipmentNames: Map<number, string>;
  departmentNames: Map<number, string>;
  equipmentName: (id?: number | null) => string;
  departmentName: (id?: number | null) => string;
  ready: boolean;
}

const EMPTY_EQUIPMENT: Equipment[] = [];

export function useLookups(): Lookups {
  const [equipment, setEquipment] = useState<Equipment[]>(EMPTY_EQUIPMENT);
  const [departments, setDepartments] = useState<Department[]>([]);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    let alive = true;
    void Promise.all([
      loadEquipmentOnce().catch(() => EMPTY_EQUIPMENT),
      loadDepartmentsOnce().catch(() => [] as Department[]),
    ]).then(([eq, dp]) => {
      if (!alive) return;
      setEquipment(eq);
      setDepartments(dp);
      setReady(true);
    });
    return () => {
      alive = false;
    };
  }, []);

  const equipmentNames = new Map<number, string>(
    equipment.map((e) => [e.equipmentId, e.equipmentName]),
  );
  const departmentNames = new Map<number, string>(
    departments.map((d) => [d.departmentId, d.departmentName]),
  );

  return {
    equipment,
    equipmentNames,
    departmentNames,
    equipmentName: (id) => (id == null ? "—" : (equipmentNames.get(id) ?? "—")),
    departmentName: (id) => (id == null ? "—" : (departmentNames.get(id) ?? "—")),
    ready,
  };
}
