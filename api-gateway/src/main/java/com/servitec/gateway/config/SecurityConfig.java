
package com.servitec.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

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
                        .pathMatchers(HttpMethod.POST, "/api/v1/solicitudes").hasRole("CLIENTE")
                        .pathMatchers(HttpMethod.GET, "/api/v1/solicitudes").hasAnyRole("CLIENTE", "ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/v1/solicitudes/*/asignaciones").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/v1/solicitudes/*").hasAnyRole("CLIENTE", "TECNICO", "ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/api/v1/solicitudes/*/cancelacion").hasAnyRole("CLIENTE", "ADMIN")
                        .pathMatchers(HttpMethod.PATCH,"/api/v1/solicitudes/*/inicio", "/api/v1/solicitudes/*/resolucion").hasRole("TECNICO")

                        .pathMatchers(HttpMethod.GET, "/api/v1/trabajos").hasRole("TECNICO")

                        .pathMatchers(HttpMethod.POST, "/api/v1/tecnicos")
                        .hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/v1/tecnicos").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/v1/tecnicos/*").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/api/v1/tecnicos/*").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/api/v1/tecnicos/*/desactivacion").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/v1/tecnicos/candidatos").hasRole("ADMIN")

                        .pathMatchers(HttpMethod.POST, "/api/v1/tecnicos/*/disponibilidades").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/v1/tecnicos/*/disponibilidades").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/api/v1/tecnicos/*/disponibilidades/*").hasRole("ADMIN")

                        .pathMatchers(HttpMethod.POST, "/api/v1/especialidades").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/v1/especialidades").hasAnyRole("CLIENTE", "ADMIN")

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
