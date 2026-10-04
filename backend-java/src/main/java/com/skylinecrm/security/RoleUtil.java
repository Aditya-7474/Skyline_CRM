package com.skylinecrm.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;

import java.util.Locale;
import java.util.Set;

public final class RoleUtil {
    private RoleUtil() {}

    public static String normalize(String role) {
        if (role == null) return null;
        return switch (role) {
            case "Sales Executive" -> "Agent";
            case "Accounts", "HR" -> "Employee";
            default -> role;
        };
    }

    public static MapPrincipal principal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        return (MapPrincipal) authentication.getPrincipal();
    }

    public static MapPrincipal require(Authentication auth, String... allowed) {
        MapPrincipal p = principal(auth);
        String role = normalize(p.role());
        if (!Set.of(allowed).contains(role))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Requires one of roles: " + String.join(", ", allowed));
        return p;
    }

    public record MapPrincipal(String id, String email, String role, String name) {}
}
