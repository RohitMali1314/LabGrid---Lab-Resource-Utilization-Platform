import type { Role } from "@/lib/auth";

/**
 * Frontend mirror of the Spring Boot @PreAuthorize rules.
 * A capability is only enabled when the backend endpoint exists AND the role is
 * authorized for it — we never call an endpoint the role cannot use.
 */
export type Capability =
  | "dashboard.view"
  | "equipment.view"
  | "equipment.manage"
  | "bookings.create"
  | "bookings.mine"
  | "bookings.viewAll"
  | "bookings.decide" // approve / reject
  | "bookings.operate" // in-use / complete / no-show
  | "waitlist.view"
  | "maintenance.view"
  | "maintenance.manage"
  | "maintenance.calibration"
  | "utilization.view"
  | "heatmaps.view"
  | "demand.view"
  | "optimization.view"
  | "analytics.view"
  | "reports.view"
  | "resourceSharing.view"
  | "resourceSharing.manage"
  | "billing.view"
  | "billing.manage"
  | "costAllocation.view"
  | "costAllocation.manage"
  | "costAnalysis.view"
  | "users.view"
  | "users.edit"
  | "users.delete"
  | "notifications.view";

const ALL: Role[] = [
  "STUDENT",
  "RESEARCHER",
  "LAB_TECHNICIAN",
  "LAB_MANAGER",
  "DEPARTMENT_HEAD",
  "INSTITUTION_ADMIN",
  "SYSTEM_ADMIN",
];

const ADMINS: Role[] = ["INSTITUTION_ADMIN", "SYSTEM_ADMIN"];
const STAFF: Role[] = [
  "LAB_TECHNICIAN",
  "LAB_MANAGER",
  "DEPARTMENT_HEAD",
  "INSTITUTION_ADMIN",
  "SYSTEM_ADMIN",
];
const ANALYSTS: Role[] = ["LAB_MANAGER", "DEPARTMENT_HEAD", "INSTITUTION_ADMIN", "SYSTEM_ADMIN"];
/** Roles the backend allows on GET /api/cost-allocations and /api/resource-sharing. */
const FINANCE_VIEWERS: Role[] = ["DEPARTMENT_HEAD", "INSTITUTION_ADMIN", "SYSTEM_ADMIN"];

/**
 * Mirrors the actual Spring Boot @PreAuthorize rules (Backend source of truth).
 * A capability is enabled only when the endpoint exists, the role is authorized
 * for it, and the role actually needs the feature.
 */
const MATRIX: Record<Capability, Role[]> = {
  "dashboard.view": ALL,
  "equipment.view": ALL,
  "equipment.manage": ADMINS,
  "bookings.create": ["STUDENT", "RESEARCHER"],
  "bookings.mine": ["STUDENT", "RESEARCHER"],
  // GET /api/bookings — LAB_TECHNICIAN, LAB_MANAGER, DEPARTMENT_HEAD, INSTITUTION_ADMIN, SYSTEM_ADMIN
  "bookings.viewAll": STAFF,
  // PUT /api/bookings/{id}/reject — LAB_MANAGER, DEPARTMENT_HEAD, INSTITUTION_ADMIN, SYSTEM_ADMIN
  "bookings.decide": ["LAB_MANAGER", "DEPARTMENT_HEAD", "INSTITUTION_ADMIN", "SYSTEM_ADMIN"],
  // PUT /api/bookings/{id}/{in-use,complete,no-show} — LAB_TECHNICIAN, LAB_MANAGER, SYSTEM_ADMIN
  "bookings.operate": ["LAB_TECHNICIAN", "LAB_MANAGER", "SYSTEM_ADMIN"],
  "waitlist.view": ALL,
  "maintenance.view": STAFF,
  // POST /api/maintenance and start/complete — LAB_MANAGER, LAB_TECHNICIAN only
  "maintenance.manage": ["LAB_TECHNICIAN", "LAB_MANAGER"],
  // GET /api/maintenance/calibration/pending — LAB_MANAGER, LAB_TECHNICIAN only
  "maintenance.calibration": ["LAB_TECHNICIAN", "LAB_MANAGER"],
  "utilization.view": ALL,
  "heatmaps.view": STAFF,
  "demand.view": ANALYSTS,
  "optimization.view": ANALYSTS,
  "analytics.view": STAFF,
  "reports.view": ANALYSTS,
  // GET /api/resource-sharing — DEPARTMENT_HEAD, INSTITUTION_ADMIN, SYSTEM_ADMIN
  "resourceSharing.view": FINANCE_VIEWERS,
  "resourceSharing.manage": ADMINS,
  "billing.view": FINANCE_VIEWERS,
  "billing.manage": ADMINS,
  // GET /api/cost-allocations — DEPARTMENT_HEAD, INSTITUTION_ADMIN, SYSTEM_ADMIN
  "costAllocation.view": FINANCE_VIEWERS,
  // approve / pay / cancel — INSTITUTION_ADMIN, SYSTEM_ADMIN
  "costAllocation.manage": ADMINS,
  "costAnalysis.view": FINANCE_VIEWERS,
  // GET /api/users — SYSTEM_ADMIN, INSTITUTION_ADMIN, DEPARTMENT_HEAD
  "users.view": ["DEPARTMENT_HEAD", "INSTITUTION_ADMIN", "SYSTEM_ADMIN"],
  // PUT /api/users/{id} and /status — SYSTEM_ADMIN, INSTITUTION_ADMIN
  "users.edit": ADMINS,
  // DELETE /api/users/{id} — SYSTEM_ADMIN
  "users.delete": ["SYSTEM_ADMIN"],
  "notifications.view": ALL,
};

export function can(role: Role | undefined | null, capability: Capability): boolean {
  if (!role) return false;
  return MATRIX[capability].includes(role);
}

/** Capability required to open a given app route (if any). */
export const ROUTE_CAPABILITY: { prefix: string; capability: Capability }[] = [
  { prefix: "/maintenance", capability: "maintenance.view" },
  { prefix: "/heatmaps", capability: "heatmaps.view" },
  { prefix: "/demand", capability: "demand.view" },
  { prefix: "/optimization", capability: "optimization.view" },
  { prefix: "/analytics", capability: "analytics.view" },
  { prefix: "/reports", capability: "reports.view" },
  { prefix: "/resource-sharing", capability: "resourceSharing.view" },
  { prefix: "/billing", capability: "billing.view" },
  { prefix: "/users", capability: "users.view" },
];

