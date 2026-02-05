package com.cdcrane.cloudary.files.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record PermitUsersFileAccessRequest(@NotNull UUID fileId,
                                           List<UUID> permittedUsers) {
}
