package com.cdcrane.cloudary.auth.internal;

import com.cdcrane.cloudary.auth.exceptions.handlers.CloudaryAccessDeniedHandler;
import com.cdcrane.cloudary.auth.exceptions.handlers.CloudaryAuthEntryPoint;
import com.cdcrane.cloudary.auth.filter.AccessTokenValidatorFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUseCase jwtService;
    private final CorsConfig corsConfig;

    public static final String[] PUBLIC_URIS = {
            "/error",
            "/api/v1/auth/login",
            "/api/v1/user/register",
            "/api/v1/user/verify",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout",
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {

        http.authorizeHttpRequests(requests -> requests
                .requestMatchers(PUBLIC_URIS).permitAll()
                .anyRequest().authenticated()
        );

        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.csrf(AbstractHttpConfigurer::disable);

        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.cors(c -> c.configurationSource(corsConfig));

        http.addFilterAfter(new AccessTokenValidatorFilter(jwtService), ExceptionTranslationFilter.class);

        http.exceptionHandling(eh -> eh
                .authenticationEntryPoint(new CloudaryAuthEntryPoint())
                .accessDeniedHandler(new CloudaryAccessDeniedHandler())
        );

        return http.build();

    }


}
