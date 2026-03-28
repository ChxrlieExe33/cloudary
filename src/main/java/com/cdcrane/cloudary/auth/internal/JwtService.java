package com.cdcrane.cloudary.auth.internal;

import com.cdcrane.cloudary.auth.dto.AccessJwtData;
import com.cdcrane.cloudary.auth.dto.JwtClientSessionDataDTO;
import com.cdcrane.cloudary.auth.dto.RefreshJwtData;
import com.cdcrane.cloudary.auth.dto.TokenPairResponse;
import com.cdcrane.cloudary.auth.enums.JwtTypes;
import com.cdcrane.cloudary.auth.enums.NamedJwtClaims;
import com.cdcrane.cloudary.auth.exceptions.BadAuthenticationException;
import com.cdcrane.cloudary.auth.exceptions.BadJwtException;
import com.cdcrane.cloudary.auth.exceptions.NotPermittedToRevokeAuthException;
import com.cdcrane.cloudary.auth.exceptions.TokenNotFoundException;
import com.cdcrane.cloudary.users.api.UserUseCase;
import com.cdcrane.cloudary.users.dto.UserDTO;
import com.cdcrane.cloudary.users.principal.CloudaryUserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtService implements JwtUseCase {

    private final RefreshTokenRepository refreshTokenRepo;
    private final UserUseCase userService;

    @Value("${jwt.refresh_token_storage_pepper}")
    private String refreshTokenStoragePepper;

    @Value("${jwt.access_jwt_secret}")
    private String accessSecret;

    @Value("${jwt.refresh_jwt_secret}")
    private String refreshSecret;

    private SecretKey accessSecretKey;
    private SecretKey refreshSecretKey;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.access_expiration}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh_expiration}")
    private long refreshTokenExpirationMs;

    @PostConstruct
    private void initializeSecretKeys() {

        if (accessSecret == null || accessSecret.isEmpty() || refreshSecret == null || refreshSecret.isEmpty()) {
            throw new IllegalStateException("JWT secrets must be set!");
        }

        accessSecretKey = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
        refreshSecretKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));

    }

    /**
     * Create the access JWT for a user, with a short expiry.
     * @param auth The authentication object.
     * @return The JWT and relevant data.
     */
    @Override
    public AccessJwtData createAccessJwt(Authentication auth, UUID userId) {

        Date expiration = new Date(System.currentTimeMillis() + accessTokenExpirationMs);

        String jwt =  Jwts.builder()
                .issuer(issuer)
                .subject("JWT Access token")
                .claim(NamedJwtClaims.TYPE.name(), JwtTypes.ACCESS.name())
                .claim(NamedJwtClaims.USERNAME.name(), auth.getName())
                .claim(NamedJwtClaims.USERID.name(), userId)
                .claim(NamedJwtClaims.AUTHORITIES.name(), auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).collect(Collectors.joining(",")))
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(accessSecretKey)
                .compact();

        return new AccessJwtData(jwt, auth.getName(), expiration);

    }

    /**
     * Overloaded version of this method for when you don't have an Authentication object.
     * @param username The username for the JWT.
     * @param roles The roles.
     * @return An object with the access token data.
     */
    @Override
    public AccessJwtData createAccessJwt(String username, Set<String> roles, UUID userId) {

        Date expiration = new Date(System.currentTimeMillis() + accessTokenExpirationMs);

        String jwt =  Jwts.builder()
                .issuer(issuer)
                .subject("JWT Access token")
                .claim(NamedJwtClaims.TYPE.name(), JwtTypes.ACCESS.name())
                .claim(NamedJwtClaims.USERNAME.name(), username)
                .claim(NamedJwtClaims.USERID.name(), userId)
                .claim(NamedJwtClaims.AUTHORITIES.name(), String.join(",", roles))
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(accessSecretKey)
                .compact();

        return new AccessJwtData(jwt, username, expiration);

    }

    /**
     * Create the refresh JWT for the user, allowing to get new access tokens.
     * @param userId The ID of the user.
     * @return The JWT and relevant data.
     */
    @Override
    public RefreshJwtData createRefreshJwt(UUID userId) {

        Date expiration = new Date(System.currentTimeMillis() + refreshTokenExpirationMs);
        var jti =  UUID.randomUUID(); // Token ID.

        String jwt = Jwts.builder()
                .issuer(issuer)
                .subject("JWT Refresh token")
                .claim(NamedJwtClaims.TYPE.name(), JwtTypes.REFRESH.name())
                .claim(NamedJwtClaims.JTI.name(), jti)
                .claim(NamedJwtClaims.USERID.name(), userId)
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(refreshSecretKey)
                .compact();

        return new RefreshJwtData(jwt, userId, expiration, jti);

    }


    /**
     * Verifies the integrity of the access JWT by checking the signature with the secret key.
     * Will fail right awat if someone provides their refresh token, since they use different secrets.
     * @param jwt The JWT string.
     * @return The Claims object with the user information.
     */
    @Override
    public Claims verifyAccessJwt(String jwt) {

        try {

            return Jwts.parser()
                    .verifyWith(accessSecretKey)
                    .build()
                        .parseSignedClaims(jwt)
                        .getPayload();

        } catch (ExpiredJwtException e) {
            throw new BadJwtException("Your authentication has expired, please refresh your access token.");
        } catch (Exception e) {
            throw new BadJwtException("Your token is invalid, make sure you are using your access token.");
        }
    }

    /**
     * Verifies the integrity of the refresh JWT by checking the signature with the secret key.
     * Will only work for the refresh tokens.
     * @param jwt The JWT string.
     * @return The Claims object with the refresh information.
     */
    @Override
    public Claims verifyRefreshJwt(String jwt) {

        try {

            return Jwts.parser()
                    .verifyWith(refreshSecretKey)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();

        } catch (ExpiredJwtException e) {
            throw new BadJwtException("Your refresh token has expired, please log in again.");
        } catch (Exception e) {
            throw new BadJwtException("Your refresh token is invalid or has been tampered with, please log in again.");
        }
    }

    /**
     * With the refresh token, it checks integrity, expiry and type, also if it's been revoked from the DB.
     * Then retrieves the user object from their ID, and creates a new access token for them.
     * Finally, revokes DB entry for old refresh token and creates one for the new one.
     * @param refreshToken The users current refresh token in string format.
     * @return A pair of new tokens.
     */
    @Override
    @Transactional
    public TokenPairResponse refreshBothTokens(String refreshToken) {

        // Verifies its integrity & expiry (will throw an exception if its expired), then returns the claims
        var refreshClaims = this.verifyRefreshJwt(refreshToken);

        if (!refreshClaims.get(NamedJwtClaims.TYPE.name()).equals(JwtTypes.REFRESH.name())) {
            throw new BadJwtException("You cannot use an access token to refresh.");
        }

        String tokenId = refreshClaims.get(NamedJwtClaims.JTI.name(), String.class);

        if (tokenId == null) {
            throw new BadJwtException("Refresh token was created wrong and does not contain a JTI.");
        }

        UUID jti = UUID.fromString(tokenId);

        // Check to make sure it's not been revoked
        var originalRefreshEntry = refreshTokenRepo.findByJti(jti)
                .orElseThrow(() -> new TokenNotFoundException("Refresh token not found on server, most likely revoked."));

        var userIdString = refreshClaims.get(NamedJwtClaims.USERID.name(), String.class);

        UUID userId = UUID.fromString(userIdString);

        // Need to get user data from the database since the SecurityContext won't be populated.
        UserDTO user = userService.findById(userId);

        var newAccessTokenData = this.createAccessJwt(user.username(), user.authorities(), userId);
        var newRefreshTokenData = this.createRefreshJwt(userId);

        persistNewRefreshToken(newRefreshTokenData, originalRefreshEntry.getUserAgent());
        refreshTokenRepo.delete(originalRefreshEntry);

        return new TokenPairResponse(newAccessTokenData, newRefreshTokenData);

    }

    @Override
    public void persistNewRefreshToken(RefreshJwtData refreshJwtData, String userAgent) {

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(
                    (refreshJwtData.refreshJwt() + this.refreshTokenStoragePepper)
                            .getBytes(StandardCharsets.UTF_8)
            );

            var hashedRefreshToken = Base64.getEncoder().encodeToString(hashBytes);

            RefreshTokenEntry newRefreshEntry = RefreshTokenEntry.builder()
                    .jti(refreshJwtData.jti())
                    .hashedToken(hashedRefreshToken)
                    .expiry(refreshJwtData.expiration())
                    .userId(refreshJwtData.userId())
                    .userAgent(userAgent)
                    .build();

            refreshTokenRepo.save(newRefreshEntry);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Hash type for refresh token storage is wrong!");
        }

    }

    /**
     * Delete a refresh token from the local DB, therefore invalidating it.
     * No need to check ownership since the refresh token should be stored securely on the client.
     * @param refreshToken The refresh token with no Bearer prefix.
     */
    @Override
    @Transactional
    public void invalidateRefreshToken(String refreshToken) {

        // Check integrity and get claims.
        var refreshClaims = this.verifyRefreshJwt(refreshToken);

        if (!refreshClaims.get(NamedJwtClaims.TYPE.name()).equals(JwtTypes.REFRESH.name())) {
            throw new BadJwtException("You must provide the refresh token to invalidate it, not an access token.");
        }

        UUID jti = UUID.fromString(refreshClaims.get(NamedJwtClaims.JTI.name(), String.class));

        var token = refreshTokenRepo.findByJti(jti)
                .orElseThrow(() -> new TokenNotFoundException("Refresh token not found on server, it has most likely been revoked already."));

        refreshTokenRepo.delete(token);

    }

    @Override
    @Transactional
    public void invalidateRefreshToken(UUID jti) {

        CloudaryUserPrincipal principal = (CloudaryUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal == null) {
            throw new BadAuthenticationException("Principal is null, authentication is malformed.");
        }

        var refreshToken = refreshTokenRepo.findByJti(jti)
                .orElseThrow(() -> new TokenNotFoundException("Refresh token not found on server, it has most likely been revoked already."));

        if (!refreshToken.getUserId().equals(principal.getUserId())) {
            throw new NotPermittedToRevokeAuthException("User " + principal.getUserId() + " is not permitted to revoke refresh token " + jti + " owned by user " + refreshToken.getUserId() + "since they are not the owner.");
        }

        refreshTokenRepo.delete(refreshToken);

    }

    /**
     * Get the list of active refresh tokens for a user.
     * No need to check ownership since the user ID is obtained from the JWT, which is tamper proof.
     * @param userId The ID of the user.
     * @return The list of active refresh token entries.
     */
    @Override
    public List<JwtClientSessionDataDTO> getActiveJwtsByUserId(UUID userId) {

        List<RefreshTokenEntry> activeJwts = refreshTokenRepo.findByUserId(userId);

        return activeJwts.stream()
                .filter(j -> !j.isExpired())
                .map(j -> new JwtClientSessionDataDTO(j.getJti(), j.getExpiry(), j.getUserAgent()))
                .toList();

    }

}
