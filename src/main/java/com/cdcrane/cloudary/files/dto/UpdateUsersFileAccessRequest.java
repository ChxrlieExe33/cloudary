package com.cdcrane.cloudary.files.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record UpdateUsersFileAccessRequest(@NotNull(message = "You must provide the ID of a file you own.") UUID fileId,
                                           @NotEmpty(message = "You must provide user IDs.") List<UUID> userIds) {
}
