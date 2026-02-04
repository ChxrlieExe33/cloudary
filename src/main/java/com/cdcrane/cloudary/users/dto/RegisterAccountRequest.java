package com.cdcrane.cloudary.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record RegisterAccountRequest(@NotBlank @Length(max = 150) String username,
                                     @Length(max = 150) @NotBlank String firstName,
                                     @Length(max = 150) @NotBlank String lastName,
                                     @Length(max = 150) @NotBlank @Email String email,
                                     @Length(min = 8, max = 100, message = "Password must be between 8 & 100 characters long.") String password) {
}
