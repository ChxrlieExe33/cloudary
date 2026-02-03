package com.cdcrane.cloudary.files.web;

import com.cdcrane.cloudary.files.dto.NewSavedFileDTO;
import com.cdcrane.cloudary.files.internal.FileUploadUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


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
}
