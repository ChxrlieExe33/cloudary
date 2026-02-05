package com.cdcrane.cloudary.files.dto;

import java.time.Instant;
import java.util.UUID;

public record SavedFileDTO(UUID fileId, String fileName, Long size, Instant uploadedAt) {
}
