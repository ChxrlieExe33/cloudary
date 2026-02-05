package com.cdcrane.cloudary.files.web;

import com.cdcrane.cloudary.files.dto.NewSavedFileDTO;
import com.cdcrane.cloudary.files.dto.PermitUsersFileAccessRequest;
import com.cdcrane.cloudary.files.dto.RetrievedFileDTO;
import com.cdcrane.cloudary.files.internal.FileUploadUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class UploadController {

    private final FileUploadUseCase fileUploadUseCase;

    @PostMapping
    public ResponseEntity<NewSavedFileDTO> uploadFile(@RequestParam("file") MultipartFile file) {

        var result = fileUploadUseCase.uploadFile(file);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);

    }

    @GetMapping("/{fileId}")
    public ResponseEntity<RetrievedFileDTO> retrieveFile(@PathVariable UUID fileId) {

        var result = fileUploadUseCase.retrieveFile(fileId);

        return ResponseEntity.ok(result);

    }

    @PostMapping("/permit-access")
    public ResponseEntity<Void> permitAccessToFiles(@RequestBody PermitUsersFileAccessRequest request) {

        fileUploadUseCase.grantAccessToFiles(request);

        return ResponseEntity.noContent().build();

    }
}
