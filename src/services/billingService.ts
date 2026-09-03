import api from "./api";

/**
 * Aligned with BillingController (/api/billing) and CostAllocationController
 * (/api/cost-allocations). Only endpoints that exist in the Spring Boot backend
 * are declared here — nothing is invented.
 */

export interface Billing {
  billId: number;
  bookingId: number | null;
  institutionId: number | null;
  invoiceNumber: string | null;
  subtotal: number | null;
  gst: number | null;
  grandTotal: number | null;
  status: string | null;
  generatedDate?: string | null;
  dueDate?: string | null;
  paidDate?: string | null;
  createdAt?: string | null;
}

export interface CostAllocation {
  allocationId: number;
  billId: number;
  bookingId: number | null;
  resourceShareId: number | null;
  institutionId: number;
  allocationPercentage: number | null;
  allocatedAmount: number | null;
  status: string | null;
  remarks?: string | null;
  createdAt?: string | null;
}

export interface CostAnalysis {
  institutionId: number | null;
  totalAllocations: number | null;
  totalAllocatedCost: number | null;
  paidCost: number | null;
  pendingCost: number | null;
  approvedCost: number | null;
  cancelledCost: number | null;
}

/* ---------------------------------- bills --------------------------------- */

export const listBills = () => api.get<Billing[]>("/api/billing").then((r) => r.data);

export const getBill = (billId: number) =>
  api.get<Billing>(`/api/billing/${billId}`).then((r) => r.data);

export const listInstitutionBills = (institutionId: number) =>
  api.get<Billing[]>(`/api/billing/institution/${institutionId}`).then((r) => r.data);

export const generateInvoice = (bookingId: number) =>
  api.post<Billing>(`/api/billing/generate/${bookingId}`, {}).then((r) => r.data);

export const payInvoice = (billId: number) =>
  api.put<Billing>(`/api/billing/pay/${billId}`, {}).then((r) => r.data);

export const cancelInvoice = (billId: number) =>
  api.put<Billing>(`/api/billing/cancel/${billId}`, {}).then((r) => r.data);

export const deleteBill = (billId: number) =>
  api.delete(`/api/billing/${billId}`).then((r) => r.data);

/* ------------------------------ cost allocations --------------------------- */

export const listAllocations = () =>
  api.get<CostAllocation[]>("/api/cost-allocations").then((r) => r.data);

export const listBillAllocations = (billId: number) =>
  api.get<CostAllocation[]>(`/api/cost-allocations/bill/${billId}`).then((r) => r.data);

export const approveAllocation = (allocationId: number) =>
  api.put<CostAllocation>(`/api/cost-allocations/${allocationId}/approve`, {}).then((r) => r.data);

export const payAllocation = (allocationId: number) =>
  api.put<CostAllocation>(`/api/cost-allocations/${allocationId}/pay`, {}).then((r) => r.data);

export const cancelAllocation = (allocationId: number) =>
  api.put<CostAllocation>(`/api/cost-allocations/${allocationId}/cancel`, {}).then((r) => r.data);

/* -------------------------------- cost analysis ---------------------------- */

export const getCostAnalysis = () =>
  api.get<CostAnalysis>("/api/reports/cost-analysis").then((r) => r.data);
