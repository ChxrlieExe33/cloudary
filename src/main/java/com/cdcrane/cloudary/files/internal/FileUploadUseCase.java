package com.cdcrane.cloudary.files.internal;

import com.cdcrane.cloudary.files.dto.NewSavedFileDTO;
import com.cdcrane.cloudary.files.dto.PermitUsersFileAccessRequest;
import com.cdcrane.cloudary.files.dto.RetrievedFileDTO;
import com.cdcrane.cloudary.files.dto.SavedFileDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileUploadUseCase {

    NewSavedFileDTO uploadFile(MultipartFile file);

    RetrievedFileDTO retrieveFile(UUID fileId);

    Page<SavedFileDTO> listMyFiles(Pageable pageable);

    void grantAccessToFiles(PermitUsersFileAccessRequest request);
}
