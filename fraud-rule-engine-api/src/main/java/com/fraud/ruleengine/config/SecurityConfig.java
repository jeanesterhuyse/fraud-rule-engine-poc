package com.fraud.ruleengine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Security configuration for Keycloak OAuth2/OIDC authentication
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                // Public endpoints
                .requestMatchers(
                    "/actuator/health",
                    "/actuator/health/**",
                    "/actuator/info",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html"
                ).permitAll()
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                    .decoder(jwtDecoder())
                )
            );

        return http.build();
    }

    @Bean
    public org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder() {
        // Use JWK set URI to fetch public keys from Keycloak (via Docker network)
        String jwkSetUri = "http://keycloak:8080/realms/fraud-detection/protocol/openid-connect/certs";

        org.springframework.security.oauth2.jwt.NimbusJwtDecoder jwtDecoder =
            org.springframework.security.oauth2.jwt.NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        // Don't validate issuer strictly - accept tokens from localhost:8180 (browser) or keycloak:8080 (internal)
        jwtDecoder.setJwtValidator(org.springframework.security.oauth2.jwt.JwtValidators.createDefault());

        return jwtDecoder;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:3001"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Content-Type", "Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Extract realm roles
            Collection<GrantedAuthority> realmRoles = extractRealmRoles(jwt.getClaim("realm_access"));

            // Extract resource roles (client-specific roles)
            Collection<GrantedAuthority> resourceRoles = extractResourceRoles(jwt.getClaim("resource_access"));

            // Extract scope-based authorities (default behavior)
            JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();
            Collection<GrantedAuthority> scopeAuthorities = defaultConverter.convert(jwt);

            // Combine all authorities
            return Stream.of(realmRoles, resourceRoles, scopeAuthorities)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        });

        return converter;
    }

    private Collection<GrantedAuthority> extractRealmRoles(Map<String, Object> realmAccess) {
        if (realmAccess == null) {
            return List.of();
        }

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) realmAccess.get("roles");

        if (roles == null) {
            return List.of();
        }

        return roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
            .collect(Collectors.toList());
    }

    private Collection<GrantedAuthority> extractResourceRoles(Map<String, Object> resourceAccess) {
        if (resourceAccess == null) {
            return List.of();
        }

        return resourceAccess.values().stream()
            .filter(resource -> resource instanceof Map)
            .flatMap(resource -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> resourceMap = (Map<String, Object>) resource;

                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) resourceMap.get("roles");

                if (roles == null) {
                    return Stream.empty();
                }

                return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
            })
            .collect(Collectors.toList());
    }
}
