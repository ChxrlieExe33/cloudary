package com.cdcrane.cloudary.search.listener;

import com.cdcrane.cloudary.files.events.TextSearchableFileUploadedEvent;
import com.cdcrane.cloudary.search.internal.FileContentSearchUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchEntryEventListener {

    private final FileContentSearchUseCase fileContentSearchUseCase;

    @ApplicationModuleListener
    public void handleSearchableFileUploadedEvent(TextSearchableFileUploadedEvent event) {

        fileContentSearchUseCase.saveNewEntry(event);

    }
}
