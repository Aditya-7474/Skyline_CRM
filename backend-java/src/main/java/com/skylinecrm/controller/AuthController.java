package com.skylinecrm.controller;

import com.skylinecrm.dto.AuthDtos;
import com.skylinecrm.security.RoleUtil;
import com.skylinecrm.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/login")
    public AuthDtos.LoginResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) { return auth.login(request); }

    @GetMapping("/me")
    public AuthDtos.UserPublic me(Authentication authentication) { return auth.current(RoleUtil.principal(authentication).id()); }

    @PostMapping("/logout")
    public Map<String, Boolean> logout() { return Map.of("ok", true); }

    @PostMapping("/register")
    public AuthDtos.UserPublic register(@Valid @RequestBody AuthDtos.UserCreateRequest request, Authentication authentication) {
        RoleUtil.require(authentication, "Admin"); return auth.register(request);
    }

    @PostMapping("/register-public")
    public ResponseEntity<AuthDtos.UserPublic> registerPublic(@Valid @RequestBody AuthDtos.RegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.registerPublic(request));
    }

    @GetMapping("/users")
    public List<AuthDtos.UserPublic> users(Authentication authentication) {
        RoleUtil.require(authentication, "Admin"); return auth.listUsers();
    }

    @PutMapping("/users/{id}")
    public AuthDtos.UserPublic update(@PathVariable String id, @Valid @RequestBody AuthDtos.UserUpdateRequest request, Authentication authentication) {
        RoleUtil.require(authentication, "Admin"); return auth.update(id, request);
    }

    @DeleteMapping("/users/{id}")
    public Map<String, String> delete(@PathVariable String id, Authentication authentication) {
        RoleUtil.MapPrincipal admin = RoleUtil.require(authentication, "Admin"); auth.delete(id, admin.id());
        return Map.of("deleted", id);
    }
}
