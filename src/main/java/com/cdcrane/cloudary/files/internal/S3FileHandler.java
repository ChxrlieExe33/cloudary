package com.cdcrane.cloudary.files.internal;

import com.cdcrane.cloudary.files.dto.UploadedFileData;
import com.cdcrane.cloudary.files.exceptions.S3UploadFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3FileHandler implements FileStorageHandler{

    private final S3Client s3Client; // Bean created in config::internal

    @Override
    public UploadedFileData store(MultipartFile file, UUID fileId) {

        try {

            String key = fileId.toString() + "-" + file.getOriginalFilename();

            PutObjectRequest req = PutObjectRequest.builder()
                    .bucket("cloudary-files")
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            PutObjectResponse res = s3Client.putObject(req, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return new UploadedFileData(key, res.eTag());

        } catch (IOException e) {

            log.warn("Failed to upload file {} to S3 bucket", fileId, e);
            throw new S3UploadFailedException(e.getMessage());

        }

    }

}
