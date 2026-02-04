package com.cdcrane.cloudary.files.internal;

import com.cdcrane.cloudary.files.dto.UploadedS3File;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileStorageHandler {

    UploadedS3File store(MultipartFile file, UUID fileId);

    String getFileUrl(String key);
}
