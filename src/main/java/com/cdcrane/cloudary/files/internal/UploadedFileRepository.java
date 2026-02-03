package com.cdcrane.cloudary.files.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, UUID> {
}
