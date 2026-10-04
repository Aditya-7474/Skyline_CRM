package com.skylinecrm.controller;

import com.skylinecrm.security.RoleUtil;
import com.skylinecrm.service.CrudService;
import com.skylinecrm.service.RelationalStore;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@RestController
@RequestMapping("/api")
public class OperationsController {
    private final RelationalStore store; private final CrudService crud;
    public OperationsController(RelationalStore store, CrudService crud) { this.store = store; this.crud = crud; }
    private String now() { return OffsetDateTime.now(ZoneOffset.UTC).toString(); }
    private double number(Map<String, Object> d, String key) { Object value = d.get(key); return value instanceof Number n ? n.doubleValue() : 0; }

    @GetMapping("/petty-cash/summary/report")
    public Map<String, Object> pettySummary(Authentication auth) { RoleUtil.require(auth, "Admin", "Employee"); List<Map<String, Object>> entries = store.all("petty_cash_entries"); Map<String, Double> byCategory = new LinkedHashMap<>(); double total = 0; for (Map<String, Object> d : entries) { double amount = number(d, "amount"); total += amount; byCategory.merge(String.valueOf(d.get("category")), amount, Double::sum); } return Map.of("total_spent", total, "by_category", byCategory, "opening_balance", 200000, "closing_balance", 200000 - total, "entries_count", entries.size()); }

    @GetMapping("/payments/summary/{bookingId}")
    public Map<String, Object> paymentSummary(@PathVariable String bookingId, Authentication auth) { RoleUtil.require(auth, "Admin"); Map<String, Object> booking = store.findOrNull("bookings", bookingId); if (booking == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "bookings not found"); List<Map<String, Object>> payments = store.query("payments", Map.of("booking_id", bookingId)); double paid = payments.stream().mapToDouble(d -> number(d, "amount")).sum(); return Map.of("booking_id", bookingId, "customer_name", booking.get("customer_name"), "total_cost", number(booking, "total_price"), "paid", paid, "pending", number(booking, "total_price") - paid, "history", payments); }

    @GetMapping("/payroll/salary-runs")
    public List<Map<String, Object>> salaryRuns(@RequestParam Map<String, String> params, Authentication auth) { RoleUtil.require(auth, "Admin"); return params.get("month") == null ? store.all("salary_runs") : store.query("salary_runs", Map.of("month", params.get("month"))); }
    @PostMapping("/payroll/salary-runs")
    public Map<String, Object> createSalary(@RequestBody Map<String, Object> body, Authentication auth) { RoleUtil.require(auth, "Admin"); return crud.create("salary-runs", body, RoleUtil.principal(auth)); }
    @PutMapping("/payroll/salary-runs/{id}")
    public Map<String, Object> updateSalary(@PathVariable String id, @RequestBody Map<String, Object> body, Authentication auth) { RoleUtil.require(auth, "Admin"); return store.update("salary_runs", id, body); }
    @PostMapping("/payroll/generate/{month}")
    public Map<String, Integer> generatePayroll(@PathVariable String month, Authentication auth) { RoleUtil.require(auth, "Admin"); int generated = 0; for (Map<String, Object> e : store.all("employees")) { String employeeId = String.valueOf(e.get("id")); if (store.exists("salary_runs", Map.of("employee_id", employeeId, "month", month))) continue; double basic = number(e, "basic_salary"), hra = number(e, "hra"), allowances = number(e, "allowances"), pf = basic * .12, esic = basic < 21000 ? basic * .0075 : 0, pt = 200, gross = basic + hra + allowances, deductions = pf + esic + pt; Map<String, Object> run = new LinkedHashMap<>(); run.put("employee_id", employeeId); run.put("employee_name", e.get("name")); run.put("month", month); run.put("basic", basic); run.put("hra", hra); run.put("incentives", allowances); run.put("commission", 0); run.put("bonus", 0); run.put("deductions", deductions); run.put("pf", pf); run.put("esic", esic); run.put("professional_tax", pt); run.put("advance_recovery", 0); run.put("gross", gross); run.put("net", gross - deductions); run.put("status", "Pending"); store.insert("salary_runs", run); generated++; } return Map.of("generated", generated); }

    @GetMapping("/attendance")
    public List<Map<String, Object>> attendance(@RequestParam Map<String, String> params, Authentication auth) { RoleUtil.require(auth, "Admin"); List<Map<String, Object>> rows = store.all("attendance"); if (params.get("employee_id") != null) rows = rows.stream().filter(d -> params.get("employee_id").equals(String.valueOf(d.get("employee_id")))).toList(); if (params.get("month") != null) rows = rows.stream().filter(d -> String.valueOf(d.getOrDefault("date", "")).startsWith(params.get("month"))).toList(); return rows; }
    @PostMapping("/attendance")
    public Map<String, Object> createAttendance(@RequestBody Map<String, Object> body, Authentication auth) { RoleUtil.require(auth, "Admin"); return crud.create("attendance", body, RoleUtil.principal(auth)); }
    @PostMapping("/attendance/bulk")
    public Map<String, Integer> bulkAttendance(@RequestBody Map<String, Object> payload, Authentication auth) { RoleUtil.require(auth, "Admin"); int upserted = 0; Object raw = payload.get("entries"); if (raw instanceof List<?> entries) for (Object item : entries) if (item instanceof Map<?, ?> entry) { String employee = String.valueOf(entry.get("employee_id")), date = String.valueOf(entry.get("date")); Map<String, Object> values = new LinkedHashMap<>(); values.put("status", entry.get("status")); values.put("remarks", entry.get("remarks")); store.upsert("attendance", Map.of("employee_id", employee, "date", date), values); upserted++; } return Map.of("upserted", upserted); }

    @GetMapping("/units/{unitId}/photos")
    public List<Map<String, Object>> photos(@PathVariable String unitId, Authentication auth) { RoleUtil.principal(auth); return store.query("unit_photos", Map.of("unit_id", unitId)); }
    @PostMapping("/units/{unitId}/photos")
    public Map<String, Object> addPhoto(@PathVariable String unitId, @RequestBody Map<String, Object> body, Authentication auth) { RoleUtil.require(auth, "Admin"); Map<String, Object> unit = store.findOrNull("units", unitId); if (unit == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unit not found"); String data = String.valueOf(body.getOrDefault("data_base64", "")); if (!data.startsWith("data:image")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "data_base64 must be a data:image/* URL"); Map<String, Object> photo = new LinkedHashMap<>(); photo.put("unit_id", unitId); photo.put("unit_number", unit.get("unit_number")); photo.put("caption", body.getOrDefault("caption", "")); photo.put("taken_at", body.getOrDefault("taken_at", now())); photo.put("data_base64", data); photo.put("size", data.length()); return store.insert("unit_photos", photo); }
    @DeleteMapping("/units/photos/{photoId}")
    public Map<String, String> deletePhoto(@PathVariable String photoId, Authentication auth) { RoleUtil.require(auth, "Admin"); store.delete("unit_photos", photoId); return Map.of("deleted", photoId); }
    @GetMapping("/units/photos/counts")
    public Map<String, Long> photoCounts(Authentication auth) { RoleUtil.principal(auth); Map<String, Long> result = new HashMap<>(); store.all("unit_photos").forEach(d -> result.merge(String.valueOf(d.get("unit_id")), 1L, Long::sum)); return result; }
}
