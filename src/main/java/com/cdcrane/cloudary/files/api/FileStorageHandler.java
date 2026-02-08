package com.cdcrane.cloudary.files.api;

import com.cdcrane.cloudary.files.dto.UploadedS3File;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.UUID;

public interface FileStorageHandler {

    UploadedS3File store(MultipartFile file, UUID fileId);

    void deleteFile(String key);

    ResponseInputStream<GetObjectResponse> getFileStream(String key);

    String getFileUrl(String key);
}
