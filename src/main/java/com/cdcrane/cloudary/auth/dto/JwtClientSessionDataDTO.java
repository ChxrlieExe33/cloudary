package com.cdcrane.cloudary.auth.dto;

import java.util.Date;
import java.util.UUID;

public record JwtClientSessionDataDTO(UUID jti, Date expiration, String userAgent) {
}
