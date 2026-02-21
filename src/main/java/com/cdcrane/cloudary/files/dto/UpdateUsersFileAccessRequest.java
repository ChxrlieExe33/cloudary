package com.cdcrane.cloudary.files.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record UpdateUsersFileAccessRequest(@NotNull UUID fileId,
                                           List<UUID> userIds) {
}
