package com.cdcrane.cloudary.files.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.UUID;

public interface PermittedUserRepository extends JpaRepository<PermittedUser, UUID> {

    @Modifying
    @Query("DELETE FROM PermittedUser u WHERE u.fileId = ?1 AND u.userId IN ?2")
    void deleteAllByFileIdAndUserIdIn(UUID fileId, Collection<UUID> userIds);
}
