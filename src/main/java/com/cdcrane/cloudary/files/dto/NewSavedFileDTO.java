package com.cdcrane.cloudary.files.dto;

import java.util.UUID;

public record NewSavedFileDTO(UUID fileId, String fileName) {
}
