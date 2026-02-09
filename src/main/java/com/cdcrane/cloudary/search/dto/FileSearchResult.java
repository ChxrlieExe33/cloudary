package com.cdcrane.cloudary.search.dto;

import java.util.UUID;

public record FileSearchResult(UUID fileId, String fileName, String content) {
}
