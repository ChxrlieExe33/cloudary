package com.cdcrane.cloudary.auth.dto;

import java.util.Date;

public record AccessJwtData(String jwt, String username, Date expiration) {
}
