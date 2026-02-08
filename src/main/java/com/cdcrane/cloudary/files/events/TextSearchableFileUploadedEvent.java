package com.cdcrane.cloudary.files.events;

import java.util.UUID;

public record TextSearchableFileUploadedEvent(UUID fileId, String s3Key, UUID ownerId, String fileName, String fileType) {
}
