package com.servitec.tecnicos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST,"/api/v1/tecnicos/candidatos/interno/validar-traslado").hasRole("SERVICIO_INTERNO")
                        .requestMatchers(HttpMethod.GET,"/api/v1/tecnicos/interno/keycloak/*").hasRole("SERVICIO_INTERNO")
                        .requestMatchers(HttpMethod.POST, "/api/v1/tecnicos/candidatos").hasAnyRole("ADMIN", "SERVICIO_INTERNO")
                        .requestMatchers(HttpMethod.GET, "/api/v1/tecnicos/*/disponibilidades").hasAnyRole("ADMIN", "SERVICIO_INTERNO")
                        .requestMatchers(HttpMethod.GET, "/api/v1/tecnicos/*").hasAnyRole("ADMIN", "SERVICIO_INTERNO")
                        .requestMatchers("/api/v1/tecnicos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/especialidades").hasAnyRole("CLIENTE", "ADMIN")
                        .requestMatchers("/api/v1/especialidades/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/especialidades").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,"/api/v1/reservas").hasRole("SERVICIO_INTERNO")
                        .requestMatchers(HttpMethod.POST,"/api/v1/reservas/*/liberacion").hasRole("SERVICIO_INTERNO").anyRequest().denyAll()
                )
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    private Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return jwt -> {
            List<GrantedAuthority> authorities = new ArrayList<>();

            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.get("roles") instanceof Collection<?> roles) {
                for (Object role : roles) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }
            }

            return new JwtAuthenticationToken(jwt, authorities);
        };
    }

}
