package com.cdcrane.cloudary.files.internal;

import com.cdcrane.cloudary.files.dto.*;
import com.cdcrane.cloudary.files.exceptions.CannotAddUsersToPermittedException;
import com.cdcrane.cloudary.files.exceptions.InvalidFileTypeException;
import com.cdcrane.cloudary.files.exceptions.NotPermittedToAccessFile;
import com.cdcrane.cloudary.files.exceptions.UploadedFileNotFoundException;
import com.cdcrane.cloudary.users.api.UserUseCase;
import com.cdcrane.cloudary.users.principal.CloudaryUserPrincipal;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService implements FileUploadUseCase{

    private final FileStorageHandler fileStorageHandler;
    private final UploadedFileRepository uploadedFileRepo;
    private final PermittedUserRepository permittedUserRepo;
    private final UserUseCase userService;

    @Override
    @Transactional
    public NewSavedFileDTO uploadFile(MultipartFile file) {

        var currentUserId = getUserIdFromToken();

        UUID fileId = UUID.randomUUID();

        if (!this.isValidFile(file)) throw new InvalidFileTypeException("File type is not supported. Only text and PDF files are allowed.");

        UploadedS3File data = fileStorageHandler.store(file, fileId);

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

    @Override
    public RetrievedFileDTO retrieveFile(UUID fileId) {

        UploadedFile file = uploadedFileRepo.findById(fileId)
                .orElseThrow(() -> new UploadedFileNotFoundException("File with id " + fileId + " not found."));

        UUID currentUserId = this.getUserIdFromToken();

        // Check if they are the owner first.
        boolean permitted = file.getOwnerId().equals(currentUserId);

        // Otherwise, loop through the permitted users and compare the IDs to the current user.
        if (!permitted) {
            for (var permittedUser : file.getPermittedUsers()) {
                if (permittedUser.getUserId().equals(currentUserId)) {
                    permitted = true;
                    break;
                }
            }
        }

        if (!permitted) throw new NotPermittedToAccessFile("User " + currentUserId + " is not permitted to access file " + fileId + " since they are not the owner or a permitted user.");

        String url = fileStorageHandler.getFileUrl(file.getS3Key());

        return new RetrievedFileDTO(fileId, url);

    }

    @Override
    public Page<SavedFileDTO> listMyFiles(Pageable pageable) {

        var currentUserId = this.getUserIdFromToken();

        Page<UploadedFile> files = uploadedFileRepo.findAllByOwnerIdOrderByMostRecent(currentUserId, pageable);

        return files.map(f -> new SavedFileDTO(f.getFileId(), f.getFileName(), f.getSize(), f.getUploadedAt()));

    }

    @Override
    @Transactional
    public void deleteFile(UUID fileId) {

        UploadedFile file = uploadedFileRepo.findById(fileId)
                .orElseThrow(() -> new UploadedFileNotFoundException("File with id " + fileId + " not found."));

        if (!this.getUserIdFromToken().equals(file.getOwnerId())) throw new NotPermittedToAccessFile("User " + getUserIdFromToken() + " is not permitted to delete file " + fileId + " since they are not the owner.");

        fileStorageHandler.deleteFile(file.getS3Key());

        uploadedFileRepo.deleteById(fileId);

    }

    @Override
    @Transactional
    public void grantAccessToFiles(PermitUsersFileAccessRequest request) {

        UploadedFile file = uploadedFileRepo.findById(request.fileId())
                .orElseThrow(() -> new UploadedFileNotFoundException("File with id " + request.fileId() + " not found."));

        if (!this.getUserIdFromToken().equals(file.getOwnerId())) throw new NotPermittedToAccessFile("User " + getUserIdFromToken() + " is not permitted to grant access to file " + request.fileId() + " since they are not the owner.");

        // Check that the users all exist.
        var checkedUsers = userService.checkUsersExistByIds(request.permittedUsers());
        List<UUID> nonExistingUsers = new ArrayList<>();

        for (var entry : checkedUsers.entrySet()) {
            // If false, add to the list.
            if (!entry.getValue()) nonExistingUsers.add(entry.getKey());
        }

        if (!nonExistingUsers.isEmpty()) throw new CannotAddUsersToPermittedException("Users " + nonExistingUsers + " do not exist, so they cannot be granted permission. Please check the IDs and try again.");

        // Get the existing permitted users (No N+1 since permittedUsers is fetched by EntityGraph).
        List<UUID> alreadyPermittedUsers = file.getPermittedUsers().stream()
                .map(PermittedUser::getUserId)
                .toList();

        // Create the permissions.
        List<PermittedUser> userPermissions = new ArrayList<>();
        for (var userId : request.permittedUsers()) {

            if (alreadyPermittedUsers.contains(userId)) continue; // Don't duplicate entries, no need to throw an exception.

            PermittedUser permission = PermittedUser.builder()
                    .userId(userId)
                    .fileId(file.getFileId())
                    .build();

            userPermissions.add(permission);

        }

        if (!userPermissions.isEmpty()) permittedUserRepo.saveAll(userPermissions); // Only cause the DB query if there are actually values.

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
