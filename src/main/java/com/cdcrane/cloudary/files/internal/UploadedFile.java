package com.cdcrane.cloudary.files.internal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "uploaded_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID fileId;

    private UUID ownerId;

    private String fileName;

    private String contentType;

    private String s3Key;

    private Long size;

    private Instant uploadedAt;

    private String s3Etag;

    @OneToMany(mappedBy = "fileId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PermittedUser> permittedUsers; // Set of other users permitted read-access to the file.

}
