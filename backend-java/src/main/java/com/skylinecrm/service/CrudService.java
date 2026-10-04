package com.skylinecrm.service;

import com.skylinecrm.security.RoleUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Predicate;

@Service
public class CrudService {
    private static final Map<String, String> TABLES = Map.ofEntries(
            Map.entry("leads", "leads"), Map.entry("followups", "follow_ups"), Map.entry("units", "units"),
            Map.entry("site-visits", "site_visits"), Map.entry("negotiations", "negotiations"), Map.entry("bookings", "bookings"),
            Map.entry("documents", "documents"), Map.entry("loans", "loans"), Map.entry("agreements", "agreements"),
            Map.entry("payments", "payments"), Map.entry("possessions", "possessions"), Map.entry("support", "support_tickets"),
            Map.entry("employees", "employees"), Map.entry("vendors", "vendors"), Map.entry("purchase-orders", "purchase_orders"),
            Map.entry("vendor-bills", "vendor_bills"), Map.entry("vendor-payments", "vendor_payments"), Map.entry("petty-cash", "petty_cash_entries"),
            Map.entry("salary-runs", "salary_runs"), Map.entry("attendance", "attendance"));

    private final RelationalStore store;
    public CrudService(RelationalStore store) { this.store = store; }
    public String table(String module) { return Optional.ofNullable(TABLES.get(module)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown module")); }

    public List<Map<String, Object>> list(String module, Map<String, String> params, RoleUtil.MapPrincipal user) {
        String table = table(module);
        List<Map<String, Object>> rows = new ArrayList<>(store.all(table));
        Predicate<Map<String, Object>> predicate = row -> true;
        for (Map.Entry<String, String> e : params.entrySet()) {
            String key = e.getKey(), value = e.getValue();
            if (value == null || value.isBlank() || Set.of("q", "month", "due").contains(key)) continue;
            predicate = predicate.and(row -> value.equals(String.valueOf(row.get(key))));
        }
        String q = params.get("q");
        if (q != null && !q.isBlank()) {
            String term = q.toLowerCase(Locale.ROOT);
            List<String> keys = switch (module) {
                case "leads" -> List.of("name", "mobile", "email");
                case "units" -> List.of("unit_number");
                case "site-visits" -> List.of("lead_name");
                case "employees" -> List.of("name", "employee_code");
                case "bookings" -> List.of("customer_name");
                default -> List.of("name");
            };
            predicate = predicate.and(row -> keys.stream().anyMatch(k -> String.valueOf(row.getOrDefault(k, "")).toLowerCase(Locale.ROOT).contains(term)));
        }
        if (params.get("month") != null) predicate = predicate.and(row -> String.valueOf(row.getOrDefault("date", "")).startsWith(params.get("month")));
        String today = java.time.LocalDate.now().toString();
        if ("today".equals(params.get("due"))) predicate = predicate.and(row -> String.valueOf(row.getOrDefault("next_date", "")).startsWith(today));
        if ("overdue".equals(params.get("due"))) predicate = predicate.and(row -> String.valueOf(row.getOrDefault("next_date", "")).compareTo(OffsetDateTime.now(ZoneOffset.UTC).toString()) < 0);
        if (user != null && "Agent".equals(RoleUtil.normalize(user.role()))) {
            if (module.equals("leads")) predicate = predicate.and(row -> user.id().equals(String.valueOf(row.get("assigned_to"))));
            if (module.equals("followups") || module.equals("site-visits")) predicate = predicate.and(row -> user.id().equals(String.valueOf(row.get("executive_id"))));
        }
        rows.removeIf(predicate.negate());
        rows.forEach(row -> {
            Map<String, Object> before = new LinkedHashMap<>(row);
            normalizeBusinessData(module, row, String.valueOf(row.get("id")));
            if (!before.equals(row) && row.get("id") != null) {
                store.update(table, String.valueOf(row.get("id")), row);
            }
        });
        if (module.equals("units")) rows.sort(Comparator.comparing(r -> String.valueOf(r.getOrDefault("unit_number", ""))));
        if (module.equals("followups")) rows.sort(Comparator.comparing(r -> String.valueOf(r.getOrDefault("next_date", "")), Comparator.reverseOrder()));
        if (module.equals("petty-cash")) rows.sort(Comparator.comparing(r -> String.valueOf(r.getOrDefault("date", "")), Comparator.reverseOrder()));
        return rows;
    }

    public Map<String, Object> get(String module, String id) { return store.find(table(module), id); }

    public Map<String, Object> create(String module, Map<String, Object> input, RoleUtil.MapPrincipal user) {
        String table = table(module); Map<String, Object> data = new LinkedHashMap<>(input); data.remove("_id");
        if (!"Admin".equals(RoleUtil.normalize(user.role()))) {
            if (module.equals("leads")) data.put("assigned_to", user.id());
            if (module.equals("followups") || module.equals("site-visits")) { data.put("executive_id", user.id()); data.put("executive_name", user.name()); }
        }
        normalizeBusinessData(module, data, null);
        if (module.equals("bookings") && !"Cancelled".equals(data.get("status"))) {
            Map<String, Object> unit = store.first("units", Map.of("id", data.get("unit_id")));
            if (unit == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unit not found");
            Map<String, Object> active = activeBookingForUnit(String.valueOf(data.get("unit_id")), null);
            if (active != null) throw new ResponseStatusException(HttpStatus.CONFLICT, "Unit " + data.get("unit_number") + " already has an active booking for " + active.getOrDefault("customer_name", "this customer"));
        }
        Map<String, Object> inserted = store.insert(table, data);
        if (module.equals("bookings") && "Confirmed".equals(data.get("status"))) setUnitStatus(data.get("unit_id"), "Sold");
        if (module.equals("vendor-payments")) recalculateBill(String.valueOf(data.get("bill_id")));
        return inserted;
    }

    public Map<String, Object> update(String module, String id, Map<String, Object> patch) {
        String table = table(module); Map<String, Object> existing = store.find(table, id);
        Map<String, Object> merged = new LinkedHashMap<>(existing);
        patch.forEach((key, value) -> { if (!Set.of("_id", "id", "created_at", "createdAt").contains(key)) merged.put(key, value); });
        normalizeBusinessData(module, merged, id);
        if (module.equals("bookings") && !"Cancelled".equals(merged.get("status"))) {
            Map<String, Object> active = activeBookingForUnit(String.valueOf(merged.get("unit_id")), id);
            if (active != null) throw new ResponseStatusException(HttpStatus.CONFLICT, "Unit " + merged.get("unit_number") + " already has an active booking for " + active.getOrDefault("customer_name", "this customer"));
        }
        Map<String, Object> result = store.update(table, id, merged);
        if (module.equals("bookings")) {
            String oldUnit = String.valueOf(existing.get("unit_id"));
            String newUnit = String.valueOf(result.get("unit_id"));
            if (!Objects.equals(oldUnit, newUnit)) releaseUnitIfUnused(oldUnit, id);
            if ("Confirmed".equals(result.get("status"))) setUnitStatus(result.get("unit_id"), "Sold");
            if ("Cancelled".equals(result.get("status"))) releaseUnitIfUnused(newUnit, id);
        }
        if (module.equals("vendor-payments")) { recalculateBill(String.valueOf(existing.get("bill_id"))); if (patch.containsKey("bill_id")) recalculateBill(String.valueOf(patch.get("bill_id"))); }
        return result;
    }

    public Map<String, Object> delete(String module, String id) {
        Map<String, Object> existing = store.find(table(module), id); Map<String, Object> result = store.delete(table(module), id);
        if (module.equals("bookings") && "Confirmed".equals(existing.get("status"))) releaseUnitIfUnused(String.valueOf(existing.get("unit_id")), id);
        if (module.equals("vendor-payments")) recalculateBill(String.valueOf(existing.get("bill_id")));
        return result;
    }

    private void setUnitStatus(Object unitId, String status) { if (unitId != null) store.update("units", String.valueOf(unitId), Map.of("status", status)); }
    private void releaseUnitIfUnused(String unitId, String excludedBookingId) {
        if (unitId == null || unitId.isBlank() || "null".equals(unitId)) return;
        boolean stillConfirmed = store.query("bookings", Map.of("unit_id", unitId)).stream()
                .anyMatch(row -> !Objects.equals(String.valueOf(row.get("id")), excludedBookingId) && "Confirmed".equals(row.get("status")));
        if (!stillConfirmed) setUnitStatus(unitId, "Available");
    }
    private Map<String, Object> activeBookingForUnit(String unitId, String excludedBookingId) {
        if (unitId == null || unitId.isBlank() || "null".equals(unitId)) return null;
        return store.query("bookings", Map.of("unit_id", unitId)).stream()
                .filter(row -> !Objects.equals(String.valueOf(row.get("id")), excludedBookingId))
                .filter(row -> !"Cancelled".equals(row.get("status")))
                .findFirst().orElse(null);
    }
    private void recalculateBill(String billId) {
        if (billId == null || "null".equals(billId)) return;
        Map<String, Object> bill = store.findOrNull("vendor_bills", billId); if (bill == null) return;
        double paid = store.query("vendor_payments", Map.of("bill_id", billId)).stream().mapToDouble(d -> number(d, "paid_amount")).sum();
        double balance = Math.max(number(bill, "bill_amount") - paid, 0); String status = balance <= 0 ? "Fully Paid" : paid > 0 ? "Partially Paid" : "Pending";
        store.update("vendor_bills", billId, Map.of("paid_amount", paid, "balance", balance, "status", status));
    }
    private double number(Map<String, Object> d, String key) { Object value = d.get(key); return value instanceof Number n ? n.doubleValue() : 0; }

    private void normalizeBusinessData(String module, Map<String, Object> data, String currentId) {
        normalizeCustomerFields(data);
        switch (module) {
            case "followups" -> { attachLead(data); data.putIfAbsent("status", "Scheduled"); }
            case "site-visits" -> attachLead(data);
            case "bookings" -> { attachLead(data); attachUnit(data); }
            case "negotiations" -> { attachLead(data); attachUnit(data); }
            case "agreements", "payments", "possessions" -> attachBooking(data);
            case "documents", "loans" -> attachCustomer(data);
            case "vendor-payments" -> attachVendorBill(data);
            default -> { }
        }
        normalizeCustomerFields(data);
    }

    private void attachCustomer(Map<String, Object> data) {
        Object raw = data.getOrDefault("customer_id", data.get("lead_id"));
        Map<String, Object> lead = resolveLead(raw);
        if (lead == null) return;
        data.put("customer_id", lead.get("id"));
        data.putIfAbsent("customer_name", readableLeadName(lead));
        data.putIfAbsent("mobile", lead.get("mobile"));
        data.putIfAbsent("customer_mobile", lead.get("mobile"));
        data.putIfAbsent("customer_email", lead.get("email"));
    }

    private void attachLead(Map<String, Object> data) {
        Map<String, Object> lead = resolveLead(data.get("lead_id"));
        if (lead == null) {
            String fallback = readableText(data.get("lead_name"));
            if (fallback.isBlank()) fallback = readableText(data.get("lead_id"));
            if (!fallback.isBlank()) {
                data.putIfAbsent("lead_name", fallback);
                if (isBlank(data.get("customer_name"))) data.put("customer_name", fallback);
            }
            return;
        }
        String name = readableLeadName(lead);
        data.put("lead_id", lead.get("id"));
        data.put("lead_name", name);
        data.putIfAbsent("customer_name", name);
        if (isBlank(data.get("customer_name"))) data.put("customer_name", name);
        if (isBlank(data.get("mobile")) && !isBlank(lead.get("mobile"))) data.put("mobile", lead.get("mobile"));
        if (isBlank(data.get("customer_mobile")) && !isBlank(lead.get("mobile"))) data.put("customer_mobile", lead.get("mobile"));
        if (isBlank(data.get("customer_email")) && !isBlank(lead.get("email"))) data.put("customer_email", lead.get("email"));
    }

    private void attachUnit(Map<String, Object> data) {
        Map<String, Object> unit = resolveUnit(data.getOrDefault("unit_id", data.get("unit_number")));
        if (unit == null) return;
        data.put("unit_id", unit.get("id"));
        data.put("unit_number", unit.get("unit_number"));
        data.put("unit_name", unit.get("unit_number"));
        data.putIfAbsent("flat_type", unit.get("flat_type"));
        data.putIfAbsent("floor", unit.get("floor"));
    }

    private void attachBooking(Map<String, Object> data) {
        Map<String, Object> booking = resolveBooking(data.get("booking_id"));
        if (booking == null) return;
        data.put("booking_id", booking.get("id"));
        data.putIfAbsent("customer_name", booking.get("customer_name"));
        data.putIfAbsent("mobile", booking.get("mobile"));
        data.putIfAbsent("customer_mobile", booking.get("customer_mobile"));
        data.putIfAbsent("customer_email", booking.get("customer_email"));
        data.putIfAbsent("project_name", booking.get("project_name"));
        data.putIfAbsent("unit_number", booking.get("unit_number"));
        data.putIfAbsent("unit_name", booking.get("unit_name"));
        data.putIfAbsent("lead_id", booking.get("lead_id"));
        attachLead(data);
    }

    private void attachVendorBill(Map<String, Object> data) {
        Map<String, Object> bill = resolveVendorBill(data.get("bill_id"));
        if (bill == null) return;
        data.put("bill_id", bill.get("id"));
        data.put("bill_number", bill.get("bill_number"));
        data.put("vendor_id", bill.get("vendor_id"));
        data.putIfAbsent("vendor_name", bill.get("vendor_name"));
    }

    private void normalizeCustomerFields(Map<String, Object> data) {
        mirror(data, "customer_name", "name");
        mirror(data, "customer_mobile", "mobile");
        mirror(data, "customer_email", "email");
        mirror(data, "unit_name", "unit_number");
        if (isBlank(data.get("project_name")) && !isBlank(data.get("project"))) data.put("project_name", data.get("project"));
    }

    private void mirror(Map<String, Object> data, String primary, String alias) {
        if (isBlank(data.get(primary)) && !isBlank(data.get(alias))) data.put(primary, data.get(alias));
        if (isBlank(data.get(alias)) && !isBlank(data.get(primary))) data.put(alias, data.get(primary));
    }

    private Map<String, Object> resolveLead(Object input) {
        return resolveFrom("leads", input, List.of("id", "name", "customer_name", "mobile", "email"));
    }

    private Map<String, Object> resolveUnit(Object input) {
        String text = String.valueOf(input == null ? "" : input).trim();
        if (text.isBlank()) return null;
        List<Map<String, Object>> rows = store.all("units");
        Optional<Map<String, Object>> exact = rows.stream().filter(row ->
                equalsCompact(row.get("id"), text) || equalsCompact(row.get("unit_number"), text) || sameDigits(row.get("unit_number"), text)
        ).findFirst();
        return exact.orElse(null);
    }

    private Map<String, Object> resolveBooking(Object input) {
        return resolveFrom("bookings", input, List.of("id", "customer_name", "lead_name", "mobile", "unit_number"));
    }

    private Map<String, Object> resolveVendorBill(Object input) {
        return resolveFrom("vendor_bills", input, List.of("id", "bill_number", "vendor_name"));
    }

    private Map<String, Object> resolveFrom(String table, Object input, List<String> keys) {
        String text = String.valueOf(input == null ? "" : input).trim();
        if (text.isBlank()) return null;
        List<Map<String, Object>> rows = store.all(table);
        for (Map<String, Object> row : rows) {
            if (keys.stream().anyMatch(key -> equalsCompact(row.get(key), text))) return row;
        }
        List<Map<String, Object>> contains = rows.stream()
                .filter(row -> keys.stream().anyMatch(key -> containsCompact(row.get(key), text)))
                .toList();
        return contains.size() == 1 ? contains.get(0) : null;
    }

    private String readableLeadName(Map<String, Object> lead) {
        Object name = lead.get("customer_name") == null ? lead.get("name") : lead.get("customer_name");
        return String.valueOf(name == null ? "" : name);
    }

    private boolean isBlank(Object value) {
        return value == null || String.valueOf(value).isBlank();
    }

    private String readableText(Object value) {
        String text = String.valueOf(value == null ? "" : value).trim();
        return text.matches("(?i)[0-9a-f]{8}-[0-9a-f-]{27,}") ? "" : text;
    }

    private boolean equalsCompact(Object left, Object right) {
        String a = compact(left), b = compact(right);
        return !a.isBlank() && a.equals(b);
    }

    private boolean containsCompact(Object left, Object right) {
        String a = compact(left), b = compact(right);
        return !a.isBlank() && !b.isBlank() && a.contains(b);
    }

    private boolean sameDigits(Object left, Object right) {
        String a = digits(left), b = digits(right);
        return !a.isBlank() && !b.isBlank() && a.equals(b);
    }

    private String compact(Object value) {
        return String.valueOf(value == null ? "" : value).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private String digits(Object value) {
        return String.valueOf(value == null ? "" : value).replaceAll("\\D", "").replaceFirst("^0+(?!$)", "");
    }
}
