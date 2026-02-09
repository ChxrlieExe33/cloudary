package com.cdcrane.cloudary.search.internal;

import com.cdcrane.cloudary.files.api.FileStorageHandler;
import com.cdcrane.cloudary.files.events.TextSearchableFileUploadedEvent;
import com.cdcrane.cloudary.search.dto.FileSearchResult;
import com.cdcrane.cloudary.users.principal.CloudaryUserPrincipal;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileContentSearchService implements FileContentSearchUseCase{

    private final FileContentEntryRepository fileContentEntryRepo;
    private final FileStorageHandler storageHandler;

    @Override
    @Transactional
    public void saveNewEntry(TextSearchableFileUploadedEvent data) {

        var contentStream = storageHandler.getFileStream(data.s3Key());

        String content;

        try {

            if (data.fileType().equals("application/pdf")) {
                content = extractTextFromPdf(contentStream);

            } else if (data.fileType().startsWith("text/")) {
                content = extractTextFromTextFile(contentStream);

            } else {

                log.warn("File {} is not a text file or PDF, so it cannot be indexed for search.", data.fileName());
                return;
            }

            FileContentEntry entry = FileContentEntry.builder()
                    .fileId(data.fileId())
                    .content(content)
                    .ownerId(data.ownerId())
                    .fileName(data.fileName())
                    .build();

            fileContentEntryRepo.save(entry);

            log.info("Successfully indexed file {} with id {} for search.", data.fileName(), data.fileId());

        } catch (IOException e) {

            log.error("Failed to extract text from file {} because of an IO error.", data.fileName(), e);
        }


    }

    @Override
    public List<FileSearchResult> searchByContent(String query) {

        UUID currentUserId = getUserIdFromToken();

        List<FileContentEntry> results = fileContentEntryRepo.searchByContent(query, currentUserId);

        return results.stream()
                .map(e -> new FileSearchResult(e.getFileId(), e.getFileName(), e.getContent()))
                .toList();

    }

    private String extractTextFromPdf(InputStream inputStream) throws IOException {

        try (PDDocument doc = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    private String extractTextFromTextFile(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private UUID getUserIdFromToken() {

        CloudaryUserPrincipal principal = (CloudaryUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal == null) {
            throw new IllegalStateException("Principal is null."); // TODO: Translate to domain exception later.
        }

        return principal.getUserId();

    }

}
