package com.skylinecrm.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class RootController {
    @GetMapping("/api/")
    public Map<String, String> root() { return Map.of("message", "SKYLINE REAL ESTATE CRM API", "status", "ok"); }
    @PostMapping("/api/seed")
    public Map<String, String> seed() { return Map.of("status", "seeded"); }
}
