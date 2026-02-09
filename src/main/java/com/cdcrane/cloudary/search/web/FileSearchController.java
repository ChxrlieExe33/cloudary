package com.cdcrane.cloudary.search.web;

import com.cdcrane.cloudary.search.dto.FileSearchResult;
import com.cdcrane.cloudary.search.internal.FileContentSearchUseCase;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class FileSearchController {

    private final FileContentSearchUseCase fileContentSearchUseCase;

    @GetMapping("/by-content")
    public ResponseEntity<List<FileSearchResult>> searchByContent(@RequestParam @NotBlank String query) {

        var results = fileContentSearchUseCase.searchByContent(query);

        return ResponseEntity.ok(results);

    }

}
