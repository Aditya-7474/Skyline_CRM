package com.skylinecrm.controller;

import com.skylinecrm.security.RoleUtil;
import com.skylinecrm.service.CrudService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class ModuleController {
    private final CrudService crud;
    public ModuleController(CrudService crud) { this.crud = crud; }

    @GetMapping("/{module}")
    public List<Map<String, Object>> list(@PathVariable String module, @RequestParam Map<String, String> params, Authentication auth) {
        RoleUtil.MapPrincipal user = RoleUtil.require(auth, readRoles(module));
        return crud.list(module, params, user);
    }

    @PostMapping("/{module}")
    public Map<String, Object> create(@PathVariable String module, @RequestBody Map<String, Object> body, Authentication auth) {
        RoleUtil.MapPrincipal user = RoleUtil.require(auth, writeRoles(module));
        return crud.create(module, body, user);
    }

    @GetMapping("/{module}/{id}")
    public Map<String, Object> get(@PathVariable String module, @PathVariable String id, Authentication auth) {
        RoleUtil.require(auth, readRoles(module)); return crud.get(module, id);
    }

    @PutMapping("/{module}/{id}")
    public Map<String, Object> update(@PathVariable String module, @PathVariable String id, @RequestBody Map<String, Object> body, Authentication auth) {
        RoleUtil.require(auth, writeRoles(module)); return crud.update(module, id, body);
    }

    @DeleteMapping("/{module}/{id}")
    public Map<String, Object> delete(@PathVariable String module, @PathVariable String id, Authentication auth) {
        RoleUtil.require(auth, deleteRoles(module)); return crud.delete(module, id);
    }

    private String[] readRoles(String module) {
        if (Set.of("employees", "attendance", "salary-runs", "vendors", "vendor-bills", "vendor-payments", "purchase-orders", "petty-cash").contains(module)) return new String[]{"Admin"};
        if (Set.of("leads", "followups", "site-visits", "bookings", "negotiations", "documents").contains(module)) return new String[]{"Admin", "Employee", "Agent"};
        if (Set.of("units", "loans", "agreements", "payments", "support").contains(module)) return new String[]{"Admin", "Employee"};
        return new String[]{"Admin"};
    }
    private String[] writeRoles(String module) {
        if (Set.of("leads", "followups", "site-visits", "bookings", "negotiations").contains(module)) return new String[]{"Admin", "Employee", "Agent"};
        if (Set.of("documents", "loans", "agreements", "payments", "support", "units").contains(module)) return new String[]{"Admin", "Employee"};
        return new String[]{"Admin"};
    }
    private String[] deleteRoles(String module) {
        if (Set.of("employees", "attendance", "salary-runs", "vendors", "vendor-bills", "vendor-payments", "purchase-orders", "petty-cash", "possessions").contains(module)) return new String[]{"Admin"};
        if (Set.of("leads", "followups", "site-visits", "bookings", "negotiations").contains(module)) return new String[]{"Admin", "Employee", "Agent"};
        if (Set.of("documents", "loans", "agreements", "payments", "support", "units").contains(module)) return new String[]{"Admin", "Employee"};
        return new String[]{"Admin"};
    }
}
