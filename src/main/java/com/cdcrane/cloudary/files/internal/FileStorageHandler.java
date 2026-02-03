package com.cdcrane.cloudary.files.internal;

import com.cdcrane.cloudary.files.dto.UploadedFileData;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileStorageHandler {

    UploadedFileData store(MultipartFile file, UUID fileId);
}
