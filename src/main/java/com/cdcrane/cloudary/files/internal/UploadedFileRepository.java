package com.cdcrane.cloudary.files.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, UUID> {

    @EntityGraph(attributePaths = {"permittedUsers"})
    Optional<UploadedFile> findById(UUID fileId);

    @Query("SELECT f FROM UploadedFile f WHERE f.ownerId = :userId ORDER BY f.uploadedAt DESC")
    Page<UploadedFile> findAllByOwnerIdOrderByMostRecent(UUID userId, Pageable pageable);
}
