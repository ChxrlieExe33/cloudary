package com.cdcrane.cloudary.auth.internal;

import com.cdcrane.cloudary.auth.dto.AccessJwtData;
import com.cdcrane.cloudary.auth.dto.RefreshJwtData;
import com.cdcrane.cloudary.auth.dto.TokenPairResponse;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.Authentication;

import java.util.Set;
import java.util.UUID;

public interface JwtUseCase {

    AccessJwtData createAccessJwt(Authentication auth, UUID userId);

    AccessJwtData createAccessJwt(String username, Set<String> roles, UUID userId);

    RefreshJwtData createRefreshJwt(UUID userId);

    Claims verifyAccessJwt(String jwt);

    Claims verifyRefreshJwt(String jwt);

    TokenPairResponse refreshBothTokens(String refreshToken);

    void persistNewRefreshToken(RefreshJwtData refreshJwtData);
}
