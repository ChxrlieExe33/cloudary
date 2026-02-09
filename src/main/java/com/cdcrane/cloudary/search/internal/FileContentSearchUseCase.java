package com.cdcrane.cloudary.search.internal;

import com.cdcrane.cloudary.files.events.TextSearchableFileUploadedEvent;
import com.cdcrane.cloudary.search.dto.FileSearchResult;

import java.util.List;

public interface FileContentSearchUseCase {

    void saveNewEntry(TextSearchableFileUploadedEvent data);

    List<FileSearchResult> searchByContent(String query);
}
