
package com.servitec.solicitudes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class TecnicosClientConfig {

    @Bean
    public RestClient tecnicosRestClient(
            RestClient.Builder builder,
            @Value("${servicios.tecnicos.url}") String url) {

        return builder
                .baseUrl(url)
                .build();
    }
}

