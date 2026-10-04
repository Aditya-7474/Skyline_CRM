package com.skylinecrm.controller;

import com.skylinecrm.security.RoleUtil;
import com.skylinecrm.service.RelationalStore;
import com.skylinecrm.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final RelationalStore store; private final UserRepository users;
    public DashboardController(RelationalStore store, UserRepository users) { this.store = store; this.users = users; }
    private RoleUtil.MapPrincipal user(Authentication auth) { return RoleUtil.principal(auth); }
    private List<Map<String, Object>> rows(String table, Predicate<Map<String, Object>> predicate) { return store.all(table).stream().filter(predicate).toList(); }
    private long count(String table, Predicate<Map<String, Object>> predicate) { return rows(table, predicate).size(); }
    private boolean oneOf(Object value, String... values) { return Arrays.asList(values).contains(String.valueOf(value)); }
    private double number(Map<String, Object> d, String key) { Object value = d.get(key); return value instanceof Number n ? n.doubleValue() : 0; }
    private Map<String, Object> clean(Map<String, Object> d) { return new LinkedHashMap<>(d); }

    @GetMapping("/kpis")
    public Map<String, Object> kpis(Authentication auth) {
        RoleUtil.MapPrincipal u = user(auth); boolean agent = "Agent".equals(RoleUtil.normalize(u.role()));
        Predicate<Map<String, Object>> leadScope = d -> !agent || u.id().equals(String.valueOf(d.get("assigned_to")));
        Predicate<Map<String, Object>> visitScope = d -> !agent || u.id().equals(String.valueOf(d.get("executive_id")));
        long totalLeads = count("leads", leadScope), converted = count("leads", leadScope.and(d -> oneOf(d.get("status"), "Qualified", "Converted")));
        return Map.of("open_visits", count("site_visits", visitScope.and(d -> oneOf(d.get("status"), "Scheduled", "Rescheduled"))),
                "hot_listings", count("units", d -> "Available".equals(d.get("status"))),
                "bookings_in_hand", count("bookings", d -> oneOf(d.get("status"), "Confirmed", "Pending")),
                "closure_rate", totalLeads == 0 ? 0 : Math.round(converted * 100.0 / totalLeads),
                "total_employees", count("employees", d -> true), "total_agents", users.findAll().stream().filter(d -> "Agent".equals(RoleUtil.normalize(d.getRole())) && "Active".equals(d.getStatus())).count());
    }

    @GetMapping("/pipeline")
    public Map<String, Object> pipeline(Authentication auth) {
        RoleUtil.MapPrincipal u = user(auth); boolean agent = "Agent".equals(RoleUtil.normalize(u.role()));
        Predicate<Map<String, Object>> scope = d -> !agent || u.id().equals(String.valueOf(d.get("assigned_to")));
        Map<String, Long> values = new LinkedHashMap<>();
        for (String status : List.of("New Lead", "Qualified", "Future Prospect", "Not Interested", "Converted")) values.put(status, count("leads", scope.and(d -> status.equals(d.get("status")))));
        Map<String, Long> sources = new LinkedHashMap<>(); rows("leads", scope).forEach(d -> sources.merge(String.valueOf(d.get("source")), 1L, Long::sum));
        return Map.of("pipeline", values, "sources", sources);
    }

    @GetMapping("/inventory")
    public Map<String, Map<String, Long>> inventory(Authentication auth) {
        Map<String, Map<String, Long>> result = new LinkedHashMap<>();
        store.all("units").forEach(d -> { String type = String.valueOf(d.get("flat_type")); String status = String.valueOf(d.get("status")); result.computeIfAbsent(type, k -> new LinkedHashMap<>(Map.of("total", 0L, "available", 0L, "sold", 0L))); result.get(type).compute("total", (k, v) -> v + 1); result.get(type).compute("Sold".equals(status) ? "sold" : "available", (k, v) -> v + 1); });
        return result;
    }

    @GetMapping("/accounts")
    public Map<String, Object> accounts(Authentication auth) {
        RoleUtil.require(auth, "Admin");
        double collected = store.all("payments").stream().mapToDouble(d -> number(d, "amount")).sum();
        double booked = rows("bookings", d -> "Confirmed".equals(d.get("status"))).stream().mapToDouble(d -> number(d, "total_price")).sum();
        double vendor = rows("vendor_bills", d -> !"Fully Paid".equals(d.get("status"))).stream().mapToDouble(d -> number(d, "balance")).sum();
        double petty = store.all("petty_cash_entries").stream().mapToDouble(d -> number(d, "amount")).sum();
        double paidSalary = rows("salary_runs", d -> "Paid".equals(d.get("status"))).stream().mapToDouble(d -> number(d, "net")).sum();
        double pendingSalary = rows("salary_runs", d -> "Pending".equals(d.get("status"))).stream().mapToDouble(d -> number(d, "net")).sum();
        String today = LocalDate.now().toString();
        double dailyCollection = store.all("payments").stream().filter(d -> String.valueOf(d.getOrDefault("payment_date", "")).startsWith(today)).mapToDouble(d -> number(d, "amount")).sum();
        double dailyExpenses = store.all("petty_cash_entries").stream().filter(d -> String.valueOf(d.getOrDefault("date", "")).startsWith(today)).mapToDouble(d -> number(d, "amount")).sum();
        return Map.of("receivables", Math.max(booked - collected, 0), "total_collected", collected, "vendor_outstanding", vendor, "salary_pending", pendingSalary, "salary_paid", paidSalary, "petty_spent", petty, "petty_balance", 200000 - petty, "daily_collection", dailyCollection, "daily_expenses", dailyExpenses, "profit_summary", collected - vendor - paidSalary - petty);
    }

    @GetMapping("/monthly-trends")
    public Map<String, Object> monthlyTrends(Authentication auth) {
        RoleUtil.require(auth, "Admin", "Employee", "Agent"); YearMonth now = YearMonth.now(); List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 5; i >= 0; i--) { YearMonth month = now.minusMonths(i); String key = month.toString(); long bookings = rows("bookings", d -> !"Cancelled".equals(d.get("status")) && String.valueOf(d.getOrDefault("booking_date", "")).startsWith(key)).size(); double collections = store.all("payments").stream().filter(d -> String.valueOf(d.getOrDefault("payment_date", "")).startsWith(key)).mapToDouble(d -> number(d, "amount")).sum(); trend.add(Map.of("month", key, "label", month.format(DateTimeFormatter.ofPattern("MMM yyyy")), "bookings", bookings, "collections", collections)); }
        Map<String, Long> sourceCounts = new LinkedHashMap<>(); store.all("leads").forEach(d -> sourceCounts.merge(String.valueOf(d.get("source")), 1L, Long::sum)); List<Map<String, Object>> sources = sourceCounts.entrySet().stream().map(e -> Map.<String, Object>of("name", e.getKey(), "value", e.getValue())).toList();
        return Map.of("trend", trend, "sources", sources);
    }

    @GetMapping("/customer/{leadId}")
    public Map<String, Object> customer(@PathVariable String leadId, Authentication auth) {
        RoleUtil.require(auth, "Admin", "Employee", "Agent"); Map<String, Object> lead = store.findOrNull("leads", leadId); if (lead == null) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Customer (lead) not found");
        List<Map<String, Object>> bookings = store.query("bookings", Map.of("lead_id", leadId)); List<String> bookingIds = bookings.stream().map(d -> String.valueOf(d.get("id"))).toList();
        List<Map<String, Object>> followups = store.query("follow_ups", Map.of("lead_id", leadId)); List<Map<String, Object>> visits = store.query("site_visits", Map.of("lead_id", leadId)); List<Map<String, Object>> negotiations = store.query("negotiations", Map.of("lead_id", leadId));
        List<Map<String, Object>> payments = store.all("payments").stream().filter(d -> bookingIds.contains(String.valueOf(d.get("booking_id")))).toList(); List<Map<String, Object>> documents = store.query("documents", Map.of("customer_id", leadId)); List<Map<String, Object>> loans = store.query("loans", Map.of("customer_id", leadId)); List<Map<String, Object>> agreements = store.all("agreements").stream().filter(d -> bookingIds.contains(String.valueOf(d.get("booking_id")))).toList(); List<Map<String, Object>> possessions = store.all("possessions").stream().filter(d -> bookingIds.contains(String.valueOf(d.get("booking_id")))).toList();
        double total = bookings.stream().filter(d -> "Confirmed".equals(d.get("status"))).mapToDouble(d -> number(d, "total_price")).sum(); double paid = payments.stream().mapToDouble(d -> number(d, "amount")).sum();
        Map<String, Object> summary = new LinkedHashMap<>(Map.of("total_price", total, "paid", paid, "pending", Math.max(total - paid, 0), "bookings_count", bookings.size(), "visits_count", visits.size(), "followups_count", followups.size()));
        Map<String, Object> response = new LinkedHashMap<>(); response.put("lead", clean(lead)); response.put("summary", summary); response.put("followups", followups); response.put("visits", visits); response.put("negotiations", negotiations); response.put("bookings", bookings); response.put("payments", payments); response.put("documents", documents); response.put("loans", loans); response.put("agreements", agreements); response.put("possessions", possessions); return response;
    }
}
