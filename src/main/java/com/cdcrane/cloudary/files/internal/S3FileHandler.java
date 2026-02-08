package com.cdcrane.cloudary.files.internal;

import com.cdcrane.cloudary.files.api.FileStorageHandler;
import com.cdcrane.cloudary.files.dto.UploadedS3File;
import com.cdcrane.cloudary.files.exceptions.S3UploadFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3FileHandler implements FileStorageHandler {

    private final S3Client s3Client; // Bean created in config::internal

    @Value( "${aws.s3.uploads-bucket-name}")
    public String bucketName;

    @Override
    public UploadedS3File store(MultipartFile file, UUID fileId) {

        try {

            String key = fileId.toString() + "-" + file.getOriginalFilename();

            PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(this.bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            PutObjectResponse res = s3Client.putObject(req, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return new UploadedS3File(key, res.eTag());

        } catch (IOException e) {

            log.warn("Failed to upload file {} to S3 bucket", fileId, e);
            throw new S3UploadFailedException(e.getMessage());

        }

    }

    @Override
    public void deleteFile(String key) {

        DeleteObjectRequest req = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.deleteObject(req);

    }

    @Override
    public ResponseInputStream<GetObjectResponse> getFileStream(String key) {

        GetObjectRequest req = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return s3Client.getObject(req);

    }

    @Override
    public String getFileUrl(String key) {

        try (S3Presigner signer = S3Presigner.builder().build()) {

            GetObjectRequest req = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignedRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(15))
                    .getObjectRequest(req)
                    .build();

            log.info("Presigned URL for file {} created.", key);

            return signer.presignGetObject(presignedRequest).url().toString();

        }

    }

}
