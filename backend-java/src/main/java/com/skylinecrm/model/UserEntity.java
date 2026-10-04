package com.skylinecrm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "users", indexes = {@Index(name = "idx_users_email", columnList = "email", unique = true), @Index(name = "idx_users_role_status", columnList = "role,status")})
public class UserEntity {
    @Id
    @Column(length = 100)
    private String id;
    @Column(nullable = false, length = 255)
    private String email;
    @Column(nullable = false, length = 255)
    private String name;
    @Column(name = "fullName", length = 255)
    private String fullName;
    @Column(nullable = false, length = 40)
    private String role;
    @Column(length = 40)
    private String phone;
    @Column(length = 40)
    private String mobile;
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    @Column(nullable = false, length = 30)
    private String status;
    @Column(name = "created_at", length = 64)
    private String createdAt;
    @Column(name = "updated_at", length = 64)
    private String updatedAt;

    public UserEntity() {}
    public String getId() { return id; } public void setId(String v) { id = v; }
    public String getEmail() { return email; } public void setEmail(String v) { email = v; }
    public String getName() { return name; } public void setName(String v) { name = v; }
    public String getFullName() { return fullName; } public void setFullName(String v) { fullName = v; }
    public String getRole() { return role; } public void setRole(String v) { role = v; }
    public String getPhone() { return phone; } public void setPhone(String v) { phone = v; }
    public String getMobile() { return mobile; } public void setMobile(String v) { mobile = v; }
    public String getPasswordHash() { return passwordHash; } public void setPasswordHash(String v) { passwordHash = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
    public String getCreatedAt() { return createdAt; } public void setCreatedAt(String v) { createdAt = v; }
    public String getUpdatedAt() { return updatedAt; } public void setUpdatedAt(String v) { updatedAt = v; }
}
