package com.cdcrane.cloudary.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank(message = "You must provide your username or email.") String usernameOrEmail,
                           @NotBlank(message = "You must provide your password.") String password) {
}
