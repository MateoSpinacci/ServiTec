
package com.servitec.solicitudes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.web.client.RestClient;

@Configuration
public class TecnicosClientConfig {

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository,
                        authorizedClientService
                );

        manager.setAuthorizedClientProvider(authorizedClientProvider);

        return manager;
    }

    @Bean
    public RestClient tecnicosRestClient(
            RestClient.Builder builder,
            @Value("${servicios.tecnicos.url}") String url,
            OAuth2AuthorizedClientManager authorizedClientManager) {

        return builder
                .baseUrl(url)
                .requestInterceptor((request, body, execution) -> {

                    OAuth2AuthorizeRequest authorizeRequest =
                            OAuth2AuthorizeRequest
                                    .withClientRegistrationId("ms-tecnicos")
                                    .principal("ms-solicitudes")
                                    .build();

                    OAuth2AuthorizedClient authorizedClient =
                            authorizedClientManager.authorize(authorizeRequest);

                    if (authorizedClient == null) {
                        throw new IllegalStateException(
                                "No se pudo obtener un token de Keycloak"
                        );
                    }

                    request.getHeaders().setBearerAuth(
                            authorizedClient.getAccessToken().getTokenValue()
                    );

                    return execution.execute(request, body);
                })
                .build();
    }
}

