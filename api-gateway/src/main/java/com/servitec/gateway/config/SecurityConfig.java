
package com.servitec.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;

@Configuration
public class SecurityConfig {

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        return ReactiveJwtDecoders.fromIssuerLocation(
                "http://localhost:8180/realms/servitec"
        );
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                new JwtRoleConverter()
        );

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange

                        // SOLICITUDES
                        .pathMatchers(HttpMethod.POST, "/api/v1/solicitudes")
                        .hasRole("CLIENTE")

                        .pathMatchers(HttpMethod.GET, "/api/v1/solicitudes")
                        .hasAnyRole("CLIENTE", "ADMIN")

                        .pathMatchers(HttpMethod.POST, "/api/v1/solicitudes/*/asignaciones")
                        .hasRole("ADMIN")

                        .pathMatchers(HttpMethod.GET, "/api/v1/solicitudes/*")
                        .hasAnyRole("CLIENTE", "TECNICO", "ADMIN")

                        .pathMatchers(HttpMethod.PATCH, "/api/v1/solicitudes/*/cancelacion")
                        .hasAnyRole("CLIENTE", "ADMIN")

                        .pathMatchers(HttpMethod.PATCH,
                                "/api/v1/solicitudes/*/inicio",
                                "/api/v1/solicitudes/*/resolucion")
                        .hasRole("TECNICO")

                        // TRABAJOS
                        .pathMatchers(HttpMethod.GET, "/api/v1/trabajos")
                        .hasRole("TECNICO")

                        // TECNICOS
                        .pathMatchers(HttpMethod.POST, "/api/v1/tecnicos")
                        .hasRole("ADMIN")

                        .pathMatchers(HttpMethod.GET, "/api/v1/tecnicos")
                        .hasRole("ADMIN")

                        .pathMatchers(HttpMethod.GET, "/api/v1/tecnicos/*")
                        .hasRole("ADMIN")

                        .pathMatchers(HttpMethod.PATCH, "/api/v1/tecnicos/*")
                        .hasRole("ADMIN")

                        .pathMatchers(HttpMethod.PATCH, "/api/v1/tecnicos/*/desactivacion")
                        .hasRole("ADMIN")

                        .pathMatchers(HttpMethod.POST, "/api/v1/tecnicos/candidatos")
                        .hasRole("ADMIN")

                        // DISPONIBILIDADES
                        .pathMatchers(HttpMethod.POST, "/api/v1/tecnicos/*/disponibilidades")
                        .hasRole("ADMIN")

                        .pathMatchers(HttpMethod.GET, "/api/v1/tecnicos/*/disponibilidades")
                        .hasRole("ADMIN")

                        .pathMatchers(HttpMethod.PATCH, "/api/v1/tecnicos/*/disponibilidades/*")
                        .hasRole("ADMIN")

                        // ESPECIALIDADES
                        .pathMatchers(HttpMethod.POST, "/api/v1/especialidades")
                        .hasRole("ADMIN")

                        .pathMatchers(HttpMethod.GET, "/api/v1/especialidades")
                        .hasAnyRole("CLIENTE", "ADMIN")

                        // CUALQUIER OTRA OPERACION QUEDA BLOQUEADA
                        .anyExchange().denyAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(
                                        new ReactiveJwtAuthenticationConverterAdapter(converter)
                                )
                        )
                )
                .build();
    }
}
