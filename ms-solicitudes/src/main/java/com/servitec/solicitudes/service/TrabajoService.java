package com.servitec.solicitudes.service;

import com.servitec.solicitudes.client.TecnicosClient;
import com.servitec.solicitudes.dto.tecnico.response.TecnicoResponse;
import com.servitec.solicitudes.entity.Solicitud;
import com.servitec.solicitudes.enums.EstadoSolicitud;
import com.servitec.solicitudes.repository.SolicitudRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class TrabajoService {
    TecnicosClient tecnicosClient;
    SolicitudRepository solicitudRepository;

    public List<Solicitud> obtenerTrabajosDelTecnico(String keycloakUserId) {
        TecnicoResponse tecnico = tecnicosClient.obtenerPorKeycloakUserId(keycloakUserId);

        return solicitudRepository.findTrabajosDelTecnico(
                tecnico.getId(),
                List.of(
                        EstadoSolicitud.ASIGNADA,
                        EstadoSolicitud.EN_CURSO
                )
        );
    }
}
