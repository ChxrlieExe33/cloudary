package com.cdcrane.cloudary.files.dto;

import java.util.UUID;

public record RetrievedFileDTO(UUID fileId, String presignedUrl) {
}
