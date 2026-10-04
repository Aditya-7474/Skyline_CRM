package com.skylinecrm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}

    public record UserCreateRequest(@Email @NotBlank String email, @NotBlank @Size(min = 8) String password,
                                    @NotBlank String name, @NotBlank String role, String phone) {}

    public record RegistrationRequest(@NotBlank @Size(min = 2) String fullName, @Email @NotBlank String email,
                                      @NotBlank String mobile, @NotBlank @Size(min = 8) String password,
                                      @NotBlank @Size(min = 8) String confirmPassword, @NotBlank String role) {}

    public record UserUpdateRequest(@Email String email, @Size(min = 2) String name, String role,
                                    String phone, String status, @Size(min = 8) String password) {}

    public record UserPublic(String id, String email, String name, String role, String phone,
                             String fullName, String mobile, String status, String created_at, String updated_at) {}

    public record LoginResponse(String token, UserPublic user) {}
}
