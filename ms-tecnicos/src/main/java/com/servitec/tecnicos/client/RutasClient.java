package com.servitec.tecnicos.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RutasClient {

    RestClient restClient;
    String apiKey;

    public RutasClient(
            RestClient.Builder builder,
            @Value("${openrouteservice.base-url}") String baseUrl,
            @Value("${openrouteservice.api-key}") String apiKey
    ) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();

        this.apiKey = apiKey;
    }

    public double calcularDuracionSegundos(
            Double latitudOrigen,
            Double longitudOrigen,
            Double latitudDestino,
            Double longitudDestino
    ) {
        Map<String, Object> body = Map.of(
                "coordinates", List.of(
                        List.of(longitudOrigen, latitudOrigen),
                        List.of(longitudDestino, latitudDestino)
                )
        );

        JsonNode respuesta = restClient.post()
                .uri("/v2/directions/driving-car/json")
                .header("Authorization", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        if (respuesta == null
                || !respuesta.path("routes").isArray()
                || respuesta.path("routes").isEmpty()
                || !respuesta.path("routes").get(0)
                .path("summary").path("duration").isNumber()) {
            throw new IllegalStateException(
                    "OpenRouteService devolvió una respuesta inválida"
            );
        }

        return respuesta.path("routes")
                .get(0)
                .path("summary")
                .path("duration")
                .asDouble();
    }
}