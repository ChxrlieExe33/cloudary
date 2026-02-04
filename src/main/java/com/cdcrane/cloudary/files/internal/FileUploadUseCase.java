package com.cdcrane.cloudary.files.internal;

import com.cdcrane.cloudary.files.dto.NewSavedFileDTO;
import com.cdcrane.cloudary.files.dto.RetrievedFileDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileUploadUseCase {

    NewSavedFileDTO uploadFile(MultipartFile file);

    RetrievedFileDTO retrieveFile(UUID fileId);
}
