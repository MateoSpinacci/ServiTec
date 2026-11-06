
package com.servitec.solicitudes.client;

import com.servitec.solicitudes.dto.tecnico.response.TecnicoResponse;
import com.servitec.solicitudes.dto.disponibilidad.response.DisponibilidadResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.List;

@Component
public class TecnicosClient {

    private final RestClient restClient;

    public TecnicosClient(RestClient tecnicosRestClient) {
        this.restClient = tecnicosRestClient;
    }

    public TecnicoResponse obtenerPorId(Long tecnicoId) {
        try {
            return restClient
                    .get()
                    .uri("/api/v1/tecnicos/{id}", tecnicoId)
                    .retrieve()
                    .body(TecnicoResponse.class);

        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "El técnico indicado no existe en ms-tecnicos"
            );

        } catch (HttpServerErrorException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "ms-tecnicos respondió con un error interno"
                );

        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo conectar con ms-tecnicos"
            );
        }
    }

    public List<DisponibilidadResponse> obtenerDisponibilidades(Long tecnicoId) {
        try {
            return restClient
                    .get()
                    .uri("/api/v1/tecnicos/{id}/disponibilidades", tecnicoId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<
                            List<DisponibilidadResponse>>() {});

        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "El técnico indicado no existe en ms-tecnicos"
            );

        } catch (HttpServerErrorException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "ms-tecnicos respondió con un error interno"
            );

        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo consultar la disponibilidad en ms-tecnicos"
            );
        }
    }
}

