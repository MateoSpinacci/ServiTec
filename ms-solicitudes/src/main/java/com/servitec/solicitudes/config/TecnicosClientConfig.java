package com.servitec.solicitudes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.web.client.RestClient;

@Configuration
public class TecnicosClientConfig {

    @Bean
    public RestClient tecnicosRestClient(RestClient.Builder builder, @Value("${servicios.tecnicos.url}") String url,
            OAuth2AuthorizedClientManager authorizedClientManager) {
        return builder
                .baseUrl(url)
                .requestInterceptor((request, body, execution) -> {
                    OAuth2AuthorizeRequest authorizeRequest =
                            OAuth2AuthorizeRequest
                                    .withClientRegistrationId("tecnicos-service")
                                    .principal("ms-solicitudes")
                                    .build();

                    OAuth2AuthorizedClient client = authorizedClientManager.authorize(authorizeRequest);

                    if (client == null) {
                        throw new IllegalStateException(
                                "No se pudo autorizar ms-solicitudes"
                        );
                    }

                    request.getHeaders().setBearerAuth(client.getAccessToken().getTokenValue());

                    return execution.execute(request, body);
                })
                .build();
    }
}

