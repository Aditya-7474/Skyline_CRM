package com.skylinecrm.service;

import com.skylinecrm.dto.AuthDtos;
import com.skylinecrm.model.UserEntity;
import com.skylinecrm.repository.UserRepository;
import com.skylinecrm.security.JwtService;
import com.skylinecrm.security.RoleUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwtService) { this.users = users; this.encoder = encoder; this.jwtService = jwtService; }

    public AuthDtos.LoginResponse login(AuthDtos.LoginRequest request) {
        UserEntity user = users.findByEmailIgnoreCase(request.email()).orElse(null);
        if (user == null || !"Active".equals(user.getStatus()) || !encoder.matches(request.password(), user.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        String role = RoleUtil.normalize(user.getRole());
        String token = jwtService.create(user.getId(), user.getEmail(), role, user.getName());
        return new AuthDtos.LoginResponse(token, publicUser(user));
    }

    public AuthDtos.UserPublic register(AuthDtos.UserCreateRequest request) {
        String email = request.email().toLowerCase(Locale.ROOT);
        if (users.findByEmailIgnoreCase(email).isPresent()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        String role = RoleUtil.normalize(request.role());
        if (!Set.of("Admin", "Employee", "Agent").contains(role)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role");
        UserEntity user = baseUser(email, request.name(), request.phone(), role, request.password());
        users.save(user);
        return publicUser(user);
    }

    public AuthDtos.UserPublic registerPublic(AuthDtos.RegistrationRequest request) {
        if (!request.password().equals(request.confirmPassword())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        String role = RoleUtil.normalize(request.role());
        if (!Set.of("Employee", "Agent").contains(role)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Public registration is limited to Employee or Agent");
        String email = request.email().toLowerCase(Locale.ROOT);
        if (users.findByEmailIgnoreCase(email).isPresent()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        String mobile = request.mobile().replaceAll("\\D", "");
        if (!mobile.matches("[6789]\\d{9}")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid 10-digit mobile number");
        UserEntity user = baseUser(email, request.fullName().trim().replaceAll("\\s+", " "), mobile, role, request.password());
        users.save(user);
        return publicUser(user);
    }

    public List<AuthDtos.UserPublic> listUsers() { return users.findAll().stream().map(this::publicUser).toList(); }

    public AuthDtos.UserPublic update(String id, AuthDtos.UserUpdateRequest request) {
        UserEntity user = findById(id);
        boolean changed = false;
        if (request.email() != null) { user.setEmail(request.email().toLowerCase(Locale.ROOT)); changed = true; }
        if (request.name() != null) { user.setName(request.name()); user.setFullName(request.name()); changed = true; }
        if (request.phone() != null) { user.setPhone(request.phone()); user.setMobile(request.phone()); changed = true; }
        if (request.role() != null) { user.setRole(RoleUtil.normalize(request.role())); changed = true; }
        if (request.status() != null) { user.setStatus(request.status()); changed = true; }
        if (request.password() != null) { user.setPasswordHash(encoder.encode(request.password())); changed = true; }
        if (!changed) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No changes supplied");
        touch(user);
        return publicUser(users.save(user));
    }

    public void delete(String id, String adminId) {
        if (id.equals(adminId)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot delete your own account");
        if (!users.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        users.deleteById(id);
    }

    public AuthDtos.UserPublic current(String id) { return publicUser(findById(id)); }
    public UserEntity findById(String id) { return users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")); }

    public AuthDtos.UserPublic publicUser(UserEntity user) {
        return new AuthDtos.UserPublic(user.getId(), user.getEmail(), user.getName(), RoleUtil.normalize(user.getRole()), user.getPhone(), user.getFullName(), user.getMobile(), user.getStatus(), user.getCreatedAt(), user.getUpdatedAt());
    }

    public UserEntity baseUser(String email, String name, String phone, String role, String password) {
        UserEntity user = new UserEntity();
        String id = UUID.randomUUID().toString(), timestamp = OffsetDateTime.now(ZoneOffset.UTC).toString();
        user.setId(id); user.setEmail(email); user.setName(name); user.setFullName(name); user.setRole(role); user.setPhone(phone); user.setMobile(phone); user.setPasswordHash(encoder.encode(password)); user.setStatus("Active");
        user.setCreatedAt(timestamp); user.setUpdatedAt(timestamp);
        return user;
    }

    private void touch(UserEntity user) {
        String timestamp = OffsetDateTime.now(ZoneOffset.UTC).toString();
        user.setUpdatedAt(timestamp);
    }
}
