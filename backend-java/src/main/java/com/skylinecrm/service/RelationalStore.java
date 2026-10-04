package com.skylinecrm.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.ResultSet;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

/** Relational module persistence while preserving the existing map-based API contract. */
@Service
public class RelationalStore {
    private static final Map<String, LinkedHashMap<String, String>> TABLE_COLUMNS = definitions();
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public RelationalStore(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
        migrateLegacyJsonTables();
    }

    public String table(String table) {
        if (!TABLE_COLUMNS.containsKey(table)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown table");
        return table;
    }

    public List<Map<String, Object>> all(String table) { return query(table, Map.of()); }

    public List<Map<String, Object>> query(String table, Map<String, ?> filters) {
        table(table);
        StringBuilder sql = new StringBuilder("SELECT ").append(selectColumns(table)).append(" FROM `").append(table).append("`");
        List<Object> args = new ArrayList<>();
        if (!filters.isEmpty()) {
            sql.append(" WHERE "); int i = 0;
            for (Map.Entry<String, ?> entry : filters.entrySet()) {
                validField(table, entry.getKey());
                if (i++ > 0) sql.append(" AND ");
                sql.append('`').append(entry.getKey()).append("` = ?"); args.add(entry.getValue());
            }
        }
        return jdbc.query(sql.toString(), args.toArray(), (rs, row) -> readRow(table, rs));
    }

    public List<Map<String, Object>> search(String table, String whereSql, List<?> args) {
        table(table);
        String sql = "SELECT " + selectColumns(table) + " FROM `" + table + "`" + (whereSql == null || whereSql.isBlank() ? "" : " WHERE " + whereSql);
        return jdbc.query(sql, args.toArray(), (rs, row) -> readRow(table, rs));
    }

    public long count(String table, String whereSql, List<?> args) {
        table(table);
        String sql = "SELECT COUNT(*) FROM `" + table + "`" + (whereSql == null || whereSql.isBlank() ? "" : " WHERE " + whereSql);
        Long count = jdbc.queryForObject(sql, args.toArray(), Long.class);
        return count == null ? 0 : count;
    }

    public Map<String, Object> find(String table, String id) {
        List<Map<String, Object>> rows = query(table, Map.of("id", id));
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, table + " not found");
        return rows.get(0);
    }

    public Map<String, Object> findOrNull(String table, String id) {
        try { return find(table, id); } catch (ResponseStatusException ex) { return null; }
    }

    public Map<String, Object> first(String table, Map<String, ?> filters) {
        List<Map<String, Object>> rows = query(table, filters); return rows.isEmpty() ? null : rows.get(0);
    }

    public boolean exists(String table, Map<String, ?> filters) { return first(table, filters) != null; }

    public Map<String, Object> insert(String table, Map<String, Object> input) {
        table(table); Map<String, Object> data = normalized(table, input);
        data.putIfAbsent("id", UUID.randomUUID().toString());
        String now = OffsetDateTime.now(ZoneOffset.UTC).toString();
        data.putIfAbsent("created_at", now); data.putIfAbsent("updated_at", now);
        writeRow(table, data, false); return data;
    }

    public Map<String, Object> update(String table, String id, Map<String, Object> patch) {
        Map<String, Object> data = find(table, id);
        for (Map.Entry<String, Object> entry : patch.entrySet()) {
            if (!Set.of("_id", "id", "created_at", "createdAt").contains(entry.getKey())) data.put(entry.getKey(), entry.getValue());
        }
        data = normalized(table, data); data.put("id", id); data.put("updated_at", OffsetDateTime.now(ZoneOffset.UTC).toString());
        writeRow(table, data, true); return data;
    }

    public Map<String, Object> upsert(String table, Map<String, ?> filters, Map<String, Object> values) {
        Map<String, Object> existing = first(table, filters);
        if (existing == null) { Map<String, Object> data = new LinkedHashMap<>(); filters.forEach(data::put); data.putAll(values); return insert(table, data); }
        return update(table, String.valueOf(existing.get("id")), values);
    }

    public Map<String, Object> delete(String table, String id) {
        find(table, id); jdbc.update("DELETE FROM `" + table(table) + "` WHERE id = ?", id); return Map.of("deleted", id);
    }

    private void writeRow(String table, Map<String, Object> data, boolean update) {
        LinkedHashMap<String, String> columns = TABLE_COLUMNS.get(table(table));
        List<String> writable = new ArrayList<>(); List<Object> values = new ArrayList<>();
        for (String metadata : List.of("id", "created_at", "updated_at")) {
            if (data.containsKey(metadata)) { writable.add(metadata); values.add(data.get(metadata)); }
        }
        for (String column : columns.keySet()) if (data.containsKey(column)) { writable.add(column); values.add(data.get(column)); }
        if (update) {
            List<String> assignments = writable.stream().filter(c -> !c.equals("id")).map(c -> "`" + c + "` = ?").toList();
            List<Object> args = new ArrayList<>(); for (String column : writable) if (!column.equals("id")) args.add(data.get(column)); args.add(data.get("id"));
            jdbc.update("UPDATE `" + table + "` SET " + String.join(", ", assignments) + " WHERE id = ?", args.toArray());
        } else {
            List<String> names = writable.stream().map(c -> "`" + c + "`").toList();
            String placeholders = String.join(", ", Collections.nCopies(names.size(), "?"));
            jdbc.update("INSERT INTO `" + table + "` (" + String.join(", ", names) + ") VALUES (" + placeholders + ")", values.toArray());
        }
    }

    private Map<String, Object> normalized(String table, Map<String, Object> input) {
        Map<String, Object> result = new LinkedHashMap<>(); Set<String> columns = TABLE_COLUMNS.get(table(table)).keySet();
        input.forEach((key, value) -> { if (columns.contains(key) || key.equals("id") || key.equals("created_at") || key.equals("updated_at")) result.put(key, value); });
        if (table.equals("leads")) {
            if (!result.containsKey("name") && result.containsKey("customer_name")) result.put("name", result.get("customer_name"));
            if (!result.containsKey("customer_name") && result.containsKey("name")) result.put("customer_name", result.get("name"));
            if (!result.containsKey("status") && result.containsKey("qualification_status")) result.put("status", result.get("qualification_status"));
            if (!result.containsKey("qualification_status") && result.containsKey("status")) result.put("qualification_status", result.get("status"));
        }
        return result;
    }

    private Map<String, Object> readRow(String table, ResultSet rs) throws java.sql.SQLException {
        Map<String, Object> result = new LinkedHashMap<>(); result.put("id", rs.getString("id"));
        for (String column : TABLE_COLUMNS.get(table).keySet()) { Object value = rs.getObject(column); if (value != null) result.put(column, value); }
        String created = rs.getString("created_at"), updated = rs.getString("updated_at");
        if (created != null) result.put("created_at", created); if (updated != null) result.put("updated_at", updated);
        return result;
    }

    private String selectColumns(String table) {
        List<String> columns = new ArrayList<>(); columns.add("`id`");
        TABLE_COLUMNS.get(table).keySet().forEach(c -> columns.add("`" + c + "`"));
        columns.add("`created_at`"); columns.add("`updated_at`"); return String.join(", ", columns);
    }

    private void validField(String table, String field) {
        if (!(field.equals("id") || field.equals("created_at") || field.equals("updated_at") || TABLE_COLUMNS.get(table).containsKey(field))) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid filter");
    }

    private void migrateLegacyJsonTables() {
        TABLE_COLUMNS.forEach((table, columns) -> {
            ensureTable(table, columns); if (!hasColumn(table, "data")) return;
            List<Map<String, Object>> rows = jdbc.query("SELECT id, data, created_at, updated_at FROM `" + table + "`", (rs, row) -> {
                Map<String, Object> record = new LinkedHashMap<>(); record.put("id", rs.getString("id")); record.put("data", rs.getString("data")); record.put("created_at", rs.getString("created_at")); record.put("updated_at", rs.getString("updated_at")); return record;
            });
            for (Map<String, Object> row : rows) {
                Map<String, Object> data = parseJson(String.valueOf(row.get("data")));
                data.putIfAbsent("id", row.get("id")); data.putIfAbsent("created_at", row.get("created_at")); data.putIfAbsent("updated_at", row.get("updated_at"));
                writeRow(table, normalized(table, data), true);
            }
            jdbc.execute("ALTER TABLE `" + table + "` DROP COLUMN `data`");
        });
    }

    private void ensureTable(String table, LinkedHashMap<String, String> columns) {
        try {
            jdbc.execute("CREATE TABLE IF NOT EXISTS `" + table + "` (id VARCHAR(100) PRIMARY KEY, created_at VARCHAR(64), updated_at VARCHAR(64))");
            for (Map.Entry<String, String> column : columns.entrySet()) if (!hasColumn(table, column.getKey())) jdbc.execute("ALTER TABLE `" + table + "` ADD COLUMN `" + column.getKey() + "` " + column.getValue());
        } catch (DataAccessException ex) { throw new IllegalStateException("Unable to prepare relational table " + table, ex); }
    }

    private boolean hasColumn(String table, String column) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?", Integer.class, table, column);
        return count != null && count > 0;
    }

    private Map<String, Object> parseJson(String json) {
        try { return mapper.readValue(json, new TypeReference<LinkedHashMap<String, Object>>() {}); }
        catch (Exception ex) { throw new IllegalStateException("Invalid legacy JSON stored in MySQL", ex); }
    }

    private static Map<String, LinkedHashMap<String, String>> definitions() {
        Map<String, LinkedHashMap<String, String>> all = new LinkedHashMap<>();
        all.put("leads", cols("name", "customer_name", "mobile", "customer_mobile", "email", "customer_email", "status", "qualification_status", "budget", "loan_required", "preferred_location", "flat_type", "timeline", "purpose", "city", "project_name", "configuration", "source", "assigned_to", "interested_property", "notes"));
        all.put("follow_ups", cols("lead_id", "lead_name", "customer_name", "mobile", "customer_mobile", "customer_email", "type", "date", "time", "next_date", "status", "remarks", "executive_id", "executive_name"));
        all.put("units", cols("unit_number", "flat_type", "wing", "floor", "carpet_area", "built_up_area", "price", "parking", "amenities", "facing", "status"));
        all.put("site_visits", cols("lead_id", "lead_name", "customer_name", "mobile", "customer_mobile", "customer_email", "project_name", "visit_date", "visit_time", "pickup_required", "status", "feedback", "executive_id", "executive_name"));
        all.put("negotiations", cols("lead_id", "lead_name", "customer_name", "mobile", "customer_mobile", "customer_email", "project_name", "unit_id", "unit_number", "unit_name", "offered_price", "discount", "special_offer", "payment_mode", "payment_reference", "approval_status", "remarks", "status"));
        all.put("bookings", cols("lead_id", "lead_name", "customer_name", "mobile", "customer_mobile", "customer_email", "project_name", "unit_id", "unit_number", "unit_name", "flat_type", "floor", "booking_amount", "total_price", "booking_date", "status"));
        all.put("payments", cols("booking_id", "lead_id", "lead_name", "customer_name", "mobile", "customer_mobile", "customer_email", "project_name", "unit_number", "unit_name", "installment_type", "total_cost", "amount", "pending_amount", "due_date", "payment_date", "mode", "reference", "remarks"));
        all.put("documents", cols("customer_id", "customer_name", "pan", "aadhaar", "passport_photo", "address_proof", "income_proof", "bank_statement", "pan_path", "pan_filename", "pan_content_type", "pan_size", "aadhaar_path", "aadhaar_filename", "aadhaar_content_type", "aadhaar_size", "passport_photo_path", "passport_photo_filename", "passport_photo_content_type", "passport_photo_size", "address_proof_path", "address_proof_filename", "address_proof_content_type", "address_proof_size", "income_proof_path", "income_proof_filename", "income_proof_content_type", "income_proof_size", "bank_statement_path", "bank_statement_filename", "bank_statement_content_type", "bank_statement_size", "remarks"));
        all.put("loans", cols("customer_id", "customer_name", "bank_name", "loan_amount", "emi", "sanction_date", "status", "remarks"));
        all.put("agreements", cols("booking_id", "lead_id", "lead_name", "customer_name", "mobile", "customer_mobile", "customer_email", "project_name", "unit_number", "unit_name", "agreement_number", "agreement_date", "stamp_duty", "registration_date", "registration_number", "agreement_value", "status", "remarks"));
        all.put("possessions", cols("booking_id", "customer_name", "unit_number", "final_inspection", "utility_connection", "key_handover", "possession_letter", "status", "handover_date"));
        all.put("support_tickets", cols("customer_name", "mobile", "email", "unit_number", "type", "subject", "description", "status", "assigned_to", "resolution"));
        all.put("employees", cols("employee_code", "name", "department", "designation", "mobile", "email", "doj", "basic_salary", "hra", "allowances", "bank_account", "ifsc", "pan", "aadhaar", "pf_number", "esic_number"));
        all.put("attendance", cols("employee_id", "employee_name", "date", "status", "check_in", "check_out", "leave_type", "overtime_hours", "remarks"));
        all.put("salary_runs", cols("employee_id", "employee_name", "month", "basic", "hra", "incentives", "commission", "bonus", "gross", "deductions", "pf", "esic", "professional_tax", "advance_recovery", "net", "status", "paid_date", "payment_mode"));
        all.put("vendors", cols("name", "company", "category", "gst", "pan", "contact_person", "mobile", "email", "bank_account", "ifsc", "address"));
        all.put("purchase_orders", cols("vendor_id", "vendor_name", "po_number", "po_date", "amount", "status", "description", "remarks"));
        all.put("vendor_bills", cols("vendor_id", "vendor_name", "bill_number", "invoice_number", "bill_date", "invoice_date", "bill_amount", "gst", "paid_amount", "balance", "status", "due_date", "remarks"));
        all.put("vendor_payments", cols("bill_id", "bill_number", "vendor_id", "vendor_name", "vendor_mobile", "payment_date", "mode", "paid_amount", "balance_amount", "payment_status", "utr", "bank", "remarks"));
        all.put("petty_cash_entries", cols("date", "voucher_number", "category", "amount", "payment_mode", "employee_name", "requested_by", "approved_by", "description", "remarks"));
        all.put("unit_photos", cols("unit_id", "unit_number", "caption", "taken_at", "data_base64", "size"));
        return all;
    }

    private static LinkedHashMap<String, String> cols(String... names) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>(); for (String name : names) result.put(name, sqlType(name)); return result;
    }

    private static String sqlType(String name) {
        if (name.equals("floor")) return "INT";
        if (Set.of("budget", "carpet_area", "built_up_area", "price", "booking_amount", "total_price", "offered_price", "discount", "amount", "total_cost", "pending_amount", "stamp_duty", "agreement_value", "loan_amount", "emi", "basic_salary", "hra", "allowances", "paid_amount", "balance", "balance_amount", "bill_amount", "gst", "basic", "incentives", "commission", "bonus", "gross", "deductions", "pf", "esic", "professional_tax", "advance_recovery", "net", "overtime_hours").contains(name)) return "DECIMAL(18,2)";
        if (Set.of("pan_size", "aadhaar_size", "passport_photo_size", "address_proof_size", "income_proof_size", "bank_statement_size", "size").contains(name)) return "BIGINT";
        if (Set.of("loan_required", "pickup_required", "final_inspection", "utility_connection", "key_handover", "possession_letter").contains(name)) return "BOOLEAN";
        if (Set.of("notes", "remarks", "feedback", "description", "resolution", "address", "amenities", "data_base64", "special_offer").contains(name)) return "TEXT";
        return "VARCHAR(500)";
    }
}
