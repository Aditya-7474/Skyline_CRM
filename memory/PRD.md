# SKYLINE REAL ESTATE CRM — Product Requirements Doc

## Original Problem
Continue building **SKYLINE REAL ESTATE CRM**, a real estate sales & operations CRM for modern property teams. 18 modules cover the full pipeline from lead capture to key handover, plus back-office (payroll, vendors, petty cash, accounts). Must match the provided dark-navy dashboard reference design.

## Architecture
- **Frontend:** React 19 + Tailwind + Lucide + Sonner (toast) + Router. `AuthContext` (Bearer JWT in `localStorage`) + `ThemeContext` (dark/light). All list-based modules share a `ListDetail` component driven by a small field-schema config.
- **Backend:** Spring Boot 3 + Spring Data MongoDB. Layered: controllers → services → MongoTemplate/repositories → JWT security filter and role guards.
- **DB:** MongoDB — existing collections and stable UUID references are preserved between `booking.unit_id`, `payment.booking_id`, `lead.assigned_to`, and related records.
- **Auth:** JWT (24hr) with 3 canonical roles: Admin, Employee, Agent. Role guard on backend + `ProtectedRoute` on frontend. Agents are auto-scoped to only see their own leads/visits/follow-ups.

## User Personas
- **Sales Executive (Agent):** field-focused. Sees only their assigned leads/visits/follow-ups. Runs the Enquiry → Qualification → Follow-up → Site Visit → Booking chain.
- **Admin:** company-wide visibility, back-office controls, user management, all modules.
- **Accounts:** payments, vendor bills, payroll processing, petty cash, company P&L dashboard.
- **HR:** employee master, attendance, payroll, petty cash approvals.

## Core Features Implemented ()
### Part A — Sales Pipeline (all functional end-to-end)
- ✅ **Client Enquiry** — 8 sources, assignable, defaults to New Lead
- ✅ **Lead Management** — qualification form (loan needed, timeline, purpose, preferred type)
- ✅ **Follow-up** — 6 channels, due-today/overdue filter, dashboard widget
- ✅ **Property** — 30 units seeded, 1/2/3/4 BHK, availability status, filter by type/status
- ✅ **Site Visit** — list+form matches reference screenshot, pickup toggle, scheduled/visited/rescheduled/no-show
- ✅ **Sales / Negotiation** — offered price, discount, management approval workflow
- ✅ **Booking** — auto-flips unit to Sold when Confirmed
- ✅ **Document Collection** — 6 KYC docs per customer (pending/collected/verified)
- ✅ **Loan Processing** — bank pipeline (Applied → Verification → Approved/Rejected → Disbursed)
- ✅ **Agreement** — stamp duty, registration date + number
- ✅ **Payment Tracking** — 4 installment types, per-booking summary (Total/Paid/Pending), 4 modes
- ✅ **Possession** — 4-step handover checklist (inspection/utility/keys/letter)
- ✅ **Customer Support** — 5 ticket types × 4 statuses

### Part B — Back-Office
- ✅ **Employees** — full master (PF/ESIC/PAN/Aadhaar, bank details, salary structure)
- ✅ **Payroll** — one-click generation for a month with auto PF (12%), ESIC (0.75%), PT (₹200), Mark Paid workflow
- ✅ **Vendors** — 11 categories, GST/PAN, contact + banking
- ✅ **Vendor Bills & Payments** — Auto-computes balance & status (Pending/Partially Paid/Fully Paid/Overdue)
- ✅ **Petty Cash** — 12 categories, opening/spent/closing summary, category-wise report
- ✅ **Accounts Dashboard** — 8 KPIs (Receivables, Vendor Outstanding, Salary Pending, Petty Balance, Daily Collection/Expenses, Profit) + Vendor Ledger table
- ✅ **Settings** — user list + Admin-only user creation form

### Cross-cutting
- ✅ Role-scoped dashboards with 4 KPIs (Open Visits, Hot Listings, Bookings in Hand, Closure Rate — all computed from real seed data)
- ✅ Dark/Light theme toggle with localStorage persistence
- ✅ Every list has search + sort + filter + Add New
- ✅ Full seed data: 5 users · 20 leads · 30 units · 20 site visits · 25 follow-ups · 19 bookings · 14 negotiations · 10 documents · 8 loans · 6 agreements · 4 possessions · 6 tickets · 7 employees · 6 vendors · 12 vendor bills · 20 petty cash entries
- ✅ Testing agent: **100% backend (28/28)** and **~95% frontend** pass. Access-denied view now renders inside Layout with a Back-to-Dashboard button.

## Prioritized Backlog

### P1
- WhatsApp reminders for scheduled site visits (deferred by user)
- SMS / Email reminder scheduler
- Field-level validation highlighting in forms (currently toast-only)

### P2 (recently completed 2026-01-03)
- ✅ **Customer 360 View** (`/customer/:leadId`) — single page showing leads, follow-ups, visits, negotiations, bookings, payments, docs, loans, agreements, possession. Click "360 view" on any lead row in Lead Management.
- ✅ **Live Sales Chart** on Dashboard — 6-month bookings & collections dual-axis line chart + Leads-by-Channel donut using recharts.
- ✅ **Salary Slip PDF** — every salary run has a `Slip` button that opens a print-optimised payslip view (`/salary-slip/:runId`) with Print / Download PDF button.
- ✅ **Purchase Order UI** (`/purchase-orders`) — full CRUD with vendor picker, GST auto-calc, and a printable A4 PO copy (`/purchase-orders/:id/print`) with T&C and signature block.
- ✅ **Attendance Grid** (`/attendance`) — calendar-style month view for all employees. Click any cell to cycle P→A→L→H→O. "Mark All Present Today" one-click. Bulk save via new `POST /api/attendance/bulk` endpoint. Weekend cells dimmed. Per-employee monthly summary.
- ✅ **Site Photo Gallery** (`/property/:unitId/photos`) — per-unit construction progress album. Drag-drop or file-pick upload with **auto client-side compression** (max 1600px, JPEG 82%), captioned thumbnails, delete, and a full-screen lightbox. Property list shows a **📷 Photos (N)** link per unit with a live count.
- Per-customer 360° timeline (all touchpoints in one scroll)
- Bulk lead import via CSV
- Native charts on Accounts dashboard (bar/line rather than KPI cards only)
- Email/SMS notifications for follow-up reminders
- Field-level validation highlighting in forms (currently toast-only)

### Nice-to-have
- Spring Boot + MySQL port (schema is already relational-shaped; DDL export as separate deliverable)
- Rate limiting on `/auth/login`
- Refresh-token flow

## Known Non-blockers
- Console hydration warning on `<option><span>` (dev instrumentation, not app code)
- Cookie is set on `/auth/login` but unused (frontend uses Bearer). Dead code — safe to remove later.
