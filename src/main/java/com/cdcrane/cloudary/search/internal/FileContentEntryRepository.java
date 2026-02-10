package com.cdcrane.cloudary.search.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileContentEntryRepository extends JpaRepository<FileContentEntry, UUID> {

    @Query(value = """
        SELECT *, ts_rank(content_tsv, plainto_tsquery('english', :query)) AS RANK
        FROM file_contents
        WHERE
            owner_id = :ownerId AND
            content_tsv @@ plainto_tsquery('english', :query)
        ORDER BY rank DESC
        LIMIT 10
    """, nativeQuery = true)
    List<FileContentEntry> searchByContentAndOwnerId(String query, UUID ownerId);

    @Query(value = """
        SELECT f FROM FileContentEntry f
        WHERE f.fileName LIKE :fileName%
        AND f.ownerId = :ownerId
    """)
    Page<FileContentEntry> searchByFileNameAndOwnerId(String fileName, UUID ownerId, Pageable pageable);
}
