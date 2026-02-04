package com.cdcrane.cloudary.files.internal;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, UUID> {

    @EntityGraph(attributePaths = {"permittedUsers"})
    Optional<UploadedFile> findById(UUID fileId);
}
