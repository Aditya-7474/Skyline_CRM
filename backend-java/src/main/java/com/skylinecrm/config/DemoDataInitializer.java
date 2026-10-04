package com.skylinecrm.config;

import com.skylinecrm.service.AuthService;
import com.skylinecrm.service.RelationalStore;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DemoDataInitializer {
    private final AuthService auth;
    private final RelationalStore store;
    public DemoDataInitializer(AuthService auth, PasswordEncoder ignored, RelationalStore store) { this.auth = auth; this.store = store; }

    @EventListener(ApplicationReadyEvent.class)
    public void ensureDemoUsers() {
        addIfMissing("admin@saivandan.com", "Admin@123", "Rakesh Sharma", "Admin", "9820011122");
        addIfMissing("employee@saivandan.com", "Employee@123", "Amit Deshmukh", "Employee", "9820011125");
        addIfMissing("agent@saivandan.com", "Agent@123", "Priya Patel", "Agent", "9820011123");
        ensureDemoLeads();
        ensureDemoOperations();
    }

    private void ensureDemoLeads() {
        addLead("Aditya Shinde", "7218724074", "aditya.shinde@gmail.com", "New Lead", 5000000);
        addLead("Rahul Patil", "9876543210", "rahul.patil@gmail.com", "Qualified", 7500000);
        addLead("Sneha Jadhav", "9123456780", "sneha.jadhav@gmail.com", "Follow Up", 4500000);
        addLead("Amit Deshmukh", "9988776655", "amit.deshmukh@gmail.com", "Site Visit", 9000000);
        addLead("Priya More", "8899776655", "priya.more@gmail.com", "Booking", 6500000);
    }

    private void addLead(String name, String mobile, String email, String status, double budget) {
        if (store.exists("leads", java.util.Map.of("mobile", mobile))) return;
        store.insert("leads", new java.util.LinkedHashMap<>(java.util.Map.of(
                "name", name, "customer_name", name, "mobile", mobile, "email", email,
                "status", status, "qualification_status", status, "budget", budget, "loan_required", false,
                "source", "Website")));
    }

    private void ensureDemoOperations() {
        List<Map<String, Object>> leads = store.all("leads");
        if (leads.isEmpty()) return;
        List<String> units = ensureDemoUnits();
        LocalDate today = LocalDate.now();

        for (int i = 1; i <= 10; i++) {
            Map<String, Object> lead = leads.get((i - 1) % leads.size());
            String leadId = String.valueOf(lead.get("id"));
            String followUpLabel = "Demo follow-up " + i;
            if (!store.exists("follow_ups", Map.of("remarks", followUpLabel))) {
                store.insert("follow_ups", record(
                        "lead_id", leadId, "type", i % 2 == 0 ? "WhatsApp" : "Phone Call",
                        "date", today.minusDays(i).toString(), "time", "10:00",
                        "next_date", today.plusDays(i % 5 + 1).toString(), "remarks", followUpLabel));
            }

            String customer = "Demo Buyer " + i;
            if (!store.exists("bookings", Map.of("customer_name", customer))) {
                store.insert("bookings", record(
                        "lead_id", leadId, "customer_name", customer, "unit_id", units.get((i - 1) % units.size()),
                        "unit_number", "D-" + String.format("%02d", i), "flat_type", i % 2 == 0 ? "2 BHK" : "1 BHK",
                        "floor", (i % 8) + 1, "booking_amount", 250000 + (i * 10000),
                        "total_price", 4500000 + (i * 250000), "booking_date", today.minusDays(i).toString(), "status", "Pending"));
            }

            String saleLabel = "Demo negotiation " + i;
            if (!store.exists("negotiations", Map.of("remarks", saleLabel))) {
                store.insert("negotiations", record(
                        "lead_id", leadId, "customer_name", customer, "unit_id", units.get((i - 1) % units.size()),
                        "offered_price", 4300000 + (i * 200000), "discount", 50000 + (i * 5000),
                        "special_offer", "Demo launch offer", "approval_status", i % 3 == 0 ? "Approved" : "Pending",
                        "remarks", saleLabel));
            }
        }

        Map<String, Object> vendor = store.first("vendors", Map.of("company", "Skyline Demo Vendors"));
        if (vendor == null) {
            vendor = store.insert("vendors", record(
                    "name", "Skyline Demo Vendor", "company", "Skyline Demo Vendors", "category", "Maintenance",
                    "contact_person", "Vikram Joshi", "mobile", "9000000010", "email", "vendor@skylinecrm.local"));
        }
        String vendorId = String.valueOf(vendor.get("id"));
        for (int i = 1; i <= 10; i++) {
            String billNumber = "DEMO-BILL-" + i;
            Map<String, Object> bill = store.first("vendor_bills", Map.of("bill_number", billNumber));
            if (bill == null) {
                bill = store.insert("vendor_bills", record(
                        "vendor_id", vendorId, "vendor_name", "Skyline Demo Vendor", "bill_number", billNumber,
                        "bill_date", today.minusDays(i).toString(), "bill_amount", 15000 + (i * 1000),
                        "paid_amount", 5000 + (i * 500), "balance", 10000 + (i * 500), "status", "Partially Paid",
                        "due_date", today.plusDays(i).toString(), "remarks", "Demo vendor bill"));
            }
            String paymentLabel = "Demo vendor payment " + i;
            if (!store.exists("vendor_payments", Map.of("remarks", paymentLabel))) {
                store.insert("vendor_payments", record(
                        "bill_id", String.valueOf(bill.get("id")), "vendor_id", vendorId, "vendor_name", "Skyline Demo Vendor",
                        "payment_date", today.minusDays(i).toString(), "mode", i % 2 == 0 ? "NEFT" : "UPI",
                        "paid_amount", 5000 + (i * 500), "utr", "DEMO-UTR-" + i, "bank", "Demo Bank", "remarks", paymentLabel));
            }
        }

        for (int i = 1; i <= 10; i++) {
            String voucher = "DEMO-PC-" + i;
            if (!store.exists("petty_cash_entries", Map.of("voucher_number", voucher))) {
                store.insert("petty_cash_entries", record(
                        "date", today.minusDays(i).toString(), "voucher_number", voucher,
                        "category", i % 2 == 0 ? "Office" : "Travel", "amount", 500 + (i * 125),
                        "payment_mode", i % 2 == 0 ? "UPI" : "Cash", "employee_name", "Amit Deshmukh",
                        "requested_by", "Amit Deshmukh", "approved_by", "Rakesh Sharma",
                        "description", "Demo petty cash entry", "remarks", "Demo expense"));
            }
        }

        ensureDemoEmployee("EMP-DEMO-001", "Amit Deshmukh", "Employee", "Operations", "9820011125");
        ensureDemoEmployee("AGT-DEMO-001", "Priya Patel", "Agent", "Sales", "9820011123");
    }

    private List<String> ensureDemoUnits() {
        List<String> ids = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            String unitNumber = "D-" + String.format("%02d", i);
            Map<String, Object> unit = store.first("units", Map.of("unit_number", unitNumber));
            if (unit == null) {
                unit = store.insert("units", record(
                        "unit_number", unitNumber, "flat_type", i % 2 == 0 ? "2 BHK" : "1 BHK",
                        "wing", "Demo Wing", "floor", (i % 8) + 1, "carpet_area", 650 + (i * 25),
                        "built_up_area", 800 + (i * 30), "price", 4500000 + (i * 250000),
                        "parking", "Yes", "facing", i % 2 == 0 ? "East" : "West", "status", "Available"));
            }
            ids.add(String.valueOf(unit.get("id")));
        }
        return ids;
    }

    private void ensureDemoEmployee(String code, String name, String designation, String department, String mobile) {
        if (store.exists("employees", Map.of("employee_code", code))) return;
        store.insert("employees", record(
                "employee_code", code, "name", name, "department", department, "designation", designation,
                "mobile", mobile, "email", name.toLowerCase().replace(" ", ".") + "@saivandan.com",
                "doj", LocalDate.now().minusMonths(8).toString(), "basic_salary", 35000,
                "hra", 7000, "allowances", 3000));
    }

    private Map<String, Object> record(Object... values) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i + 1 < values.length; i += 2) result.put(String.valueOf(values[i]), values[i + 1]);
        return result;
    }

    private void addIfMissing(String email, String password, String name, String role, String phone) {
        try { auth.login(new com.skylinecrm.dto.AuthDtos.LoginRequest(email, password)); }
        catch (Exception ignored) { try { auth.register(new com.skylinecrm.dto.AuthDtos.UserCreateRequest(email, password, name, role, phone)); } catch (Exception ignoredAgain) { } }
    }
}
