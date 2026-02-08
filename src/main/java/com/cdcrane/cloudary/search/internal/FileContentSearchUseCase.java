package com.cdcrane.cloudary.search.internal;

import com.cdcrane.cloudary.files.events.TextSearchableFileUploadedEvent;

public interface FileContentSearchUseCase {

    void saveNewEntry(TextSearchableFileUploadedEvent data);
}
