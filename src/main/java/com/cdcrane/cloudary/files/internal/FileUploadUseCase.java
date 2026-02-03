package com.cdcrane.cloudary.files.internal;

import com.cdcrane.cloudary.files.dto.NewSavedFileDTO;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadUseCase {

    NewSavedFileDTO uploadFile(MultipartFile file);
}
