
package com.servitec.tecnicos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                new JwtRoleConverter()
        );

        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // Candidatos: comunicación interna
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/tecnicos/candidatos")
                        .hasRole("SERVICIO_INTERNO")

                        // Búsqueda interna por ID de Keycloak
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/tecnicos/interno/keycloak/**")
                        .hasRole("SERVICIO_INTERNO")

                        // Técnicos: administración
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/tecnicos")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/tecnicos")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PATCH,
                                "/api/v1/tecnicos/*/desactivacion")
                        .hasRole("ADMIN")

                        // Datos de técnicos
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/tecnicos/*")
                        .hasAnyRole("CLIENTE", "TECNICO", "ADMIN",
                                "SERVICIO_INTERNO")

                        .requestMatchers(HttpMethod.PATCH,
                                "/api/v1/tecnicos/*")
                        .hasAnyRole("TECNICO", "ADMIN")

                        // Disponibilidades
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/tecnicos/*/disponibilidades")
                        .hasAnyRole("TECNICO", "ADMIN")

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/tecnicos/*/disponibilidades")
                        .hasAnyRole("CLIENTE", "TECNICO", "ADMIN",
                                "SERVICIO_INTERNO")

                        .requestMatchers(HttpMethod.PATCH,
                                "/api/v1/tecnicos/*/disponibilidades/*")
                        .hasAnyRole("TECNICO", "ADMIN")

                        // Especialidades
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/especialidades")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/especialidades")
                        .hasAnyRole("CLIENTE", "TECNICO", "ADMIN",
                                "SERVICIO_INTERNO")

                        // Reservas: comunicación interna
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/reservas")
                        .hasRole("SERVICIO_INTERNO")

                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/reservas/*/liberacion")
                        .hasRole("SERVICIO_INTERNO")

                        // Todo lo demás queda bloqueado
                        .anyRequest().denyAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(converter)
                        )
                )
                .build();
    }
}

