package com.cdcrane.cloudary.auth.dto;

public record TokenPairResponse(AccessJwtData accessData, RefreshJwtData refreshData) {
}
