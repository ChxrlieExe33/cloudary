package com.cdcrane.cloudary.files.internal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "user_file_permission")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermittedUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID permissionId;

    private UUID fileId;

    private UUID userId;
}
