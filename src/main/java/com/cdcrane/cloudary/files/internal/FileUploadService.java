package com.cdcrane.cloudary.files.internal;

import com.cdcrane.cloudary.files.dto.NewSavedFileDTO;
import com.cdcrane.cloudary.files.dto.UploadedFileData;
import com.cdcrane.cloudary.files.exceptions.InvalidFileTypeException;
import com.cdcrane.cloudary.users.principal.CloudaryUserPrincipal;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService implements FileUploadUseCase{

    private final FileStorageHandler fileStorageHandler;
    private final UploadedFileRepository uploadedFileRepo;

    @Override
    @Transactional
    public NewSavedFileDTO uploadFile(MultipartFile file) {

        var currentUserId = getUserIdFromToken();

        UUID fileId = UUID.randomUUID();

        if (!this.isValidFile(file)) throw new InvalidFileTypeException("File type is not supported. Only text and PDF files are allowed.");

        UploadedFileData data = fileStorageHandler.store(file, fileId);

        UploadedFile fileEntry = UploadedFile.builder()
                .fileId(fileId)
                .fileName(file.getOriginalFilename())
                .uploadedAt(Instant.now())
                .contentType(file.getContentType())
                .size(file.getSize())
                .permittedUsers(Set.of())
                .s3Key(data.s3Key())
                .s3Etag(data.s3Etag())
                .ownerId(currentUserId)
                .build();

        uploadedFileRepo.save(fileEntry);

        return new NewSavedFileDTO(fileId, file.getOriginalFilename());

    }

    private boolean isValidFile(MultipartFile file) {

        try {

            Tika tika = new Tika();

            var fileStream = file.getInputStream();
            String mimeType = tika.detect(fileStream);

            return mimeType.startsWith("text/") || mimeType.equals("application/pdf");

        } catch (IOException ex) {

            log.error("Failed to detect file type for file {} because input stream broke.", file.getOriginalFilename(), ex);
            return false;
        }

    }

    private UUID getUserIdFromToken() {

        CloudaryUserPrincipal principal = (CloudaryUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal == null) {
            throw new IllegalStateException("Principal is null."); // TODO: Translate to domain exception later.
        }

        return principal.getUserId();

    }
}
