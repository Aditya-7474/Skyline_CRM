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
        if (module.equals("bookings") && !"Cancelled".equals(data.get("status"))) {
            Map<String, Object> unit = store.first("units", Map.of("id", data.get("unit_id")));
            if (unit == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unit not found");
            if ("Sold".equals(unit.get("status"))) throw new ResponseStatusException(HttpStatus.CONFLICT, "This unit is already sold");
        }
        Map<String, Object> inserted = store.insert(table, data);
        if (module.equals("bookings") && "Confirmed".equals(data.get("status"))) setUnitStatus(data.get("unit_id"), "Sold");
        if (module.equals("vendor-payments")) recalculateBill(String.valueOf(data.get("bill_id")));
        return inserted;
    }

    public Map<String, Object> update(String module, String id, Map<String, Object> patch) {
        String table = table(module); Map<String, Object> existing = store.find(table, id);
        Map<String, Object> result = store.update(table, id, patch);
        if (module.equals("bookings")) {
            if ("Confirmed".equals(patch.get("status")) && !"Confirmed".equals(existing.get("status"))) setUnitStatus(existing.get("unit_id"), "Sold");
            if ("Cancelled".equals(patch.get("status")) && "Confirmed".equals(existing.get("status"))) setUnitStatus(existing.get("unit_id"), "Available");
        }
        if (module.equals("vendor-payments")) { recalculateBill(String.valueOf(existing.get("bill_id"))); if (patch.containsKey("bill_id")) recalculateBill(String.valueOf(patch.get("bill_id"))); }
        return result;
    }

    public Map<String, Object> delete(String module, String id) {
        Map<String, Object> existing = store.find(table(module), id); Map<String, Object> result = store.delete(table(module), id);
        if (module.equals("bookings") && "Confirmed".equals(existing.get("status"))) setUnitStatus(existing.get("unit_id"), "Available");
        if (module.equals("vendor-payments")) recalculateBill(String.valueOf(existing.get("bill_id")));
        return result;
    }

    private void setUnitStatus(Object unitId, String status) { if (unitId != null) store.update("units", String.valueOf(unitId), Map.of("status", status)); }
    private void recalculateBill(String billId) {
        if (billId == null || "null".equals(billId)) return;
        Map<String, Object> bill = store.findOrNull("vendor_bills", billId); if (bill == null) return;
        double paid = store.query("vendor_payments", Map.of("bill_id", billId)).stream().mapToDouble(d -> number(d, "paid_amount")).sum();
        double balance = Math.max(number(bill, "bill_amount") - paid, 0); String status = balance <= 0 ? "Fully Paid" : paid > 0 ? "Partially Paid" : "Pending";
        store.update("vendor_bills", billId, Map.of("paid_amount", paid, "balance", balance, "status", status));
    }
    private double number(Map<String, Object> d, String key) { Object value = d.get(key); return value instanceof Number n ? n.doubleValue() : 0; }
}
