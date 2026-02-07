package com.cdcrane.cloudary.auth.web;

import com.cdcrane.cloudary.auth.dto.LoginRequest;
import com.cdcrane.cloudary.auth.dto.TokenPairResponse;
import com.cdcrane.cloudary.auth.exceptions.BadJwtException;
import com.cdcrane.cloudary.auth.internal.AuthService;
import com.cdcrane.cloudary.auth.internal.JwtService;
import com.cdcrane.cloudary.users.principal.CloudaryUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final String BEARER = "Bearer ";
    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<TokenPairResponse> login(@RequestBody @Valid LoginRequest loginRequest) {

        var tokens = authService.login(loginRequest.usernameOrEmail(), loginRequest.password());

        return ResponseEntity.ok(tokens);

    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenPairResponse> refresh(@RequestHeader(name="Authorization") String refreshToken) {

        if (!refreshToken.startsWith(BEARER)) {
            throw new BadJwtException("Please follow the Bearer prefix format for tokens.");
        }

        var res = jwtService.refreshBothTokens(refreshToken.substring(BEARER.length()));

        return ResponseEntity.ok(res);

    }

    /**
     * Invalidate the refresh token provided in the Authorization header.
     * @param refreshToken The refresh token.
     * @return Nothing
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(name = "Authorization") String refreshToken) {

        if (!refreshToken.startsWith(BEARER)) {
            throw new BadJwtException("Please follow the Bearer prefix format for tokens.");
        }

        jwtService.invalidateRefreshToken(refreshToken.substring(BEARER.length()));

        return ResponseEntity.noContent().build();

    }

    @GetMapping
    public String testProtected() {

        CloudaryUserPrincipal principal = (CloudaryUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal == null) {
            return "Principal is null.";
        }

        return "You are allowed in " + SecurityContextHolder.getContext().getAuthentication().getName() + " with user-id " + principal.getUserId();
    }

}
