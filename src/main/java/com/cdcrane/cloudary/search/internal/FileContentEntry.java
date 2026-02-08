package com.cdcrane.cloudary.search.internal;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "file_contents")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class FileContentEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false, name = "file_id")
    private UUID fileId;

    @Column(nullable = false, name = "owner_id")
    private UUID ownerId;

    @Column(nullable = false, name = "file_name")
    private String fileName;

    /**
     * This column in the db will contain the
     * postgres full-text search vector, indexed with GIN.
     */
    @Column(
            name = "content_tsv",
            insertable = false,
            updatable = false
    )
    @JdbcTypeCode(SqlTypes.OTHER) // Necessary so that auto-ddl: validate doesn't cause the app to fail.
    private String contentTsv;
}
