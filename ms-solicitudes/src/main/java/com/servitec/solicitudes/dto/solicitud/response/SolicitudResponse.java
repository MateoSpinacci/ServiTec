package com.servitec.solicitudes.dto.solicitud.response;

import com.servitec.solicitudes.entity.Solicitud;
import com.servitec.solicitudes.enums.EstadoSolicitud;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SolicitudResponse {

    Long id;
    Long clienteId;
    Long tecnicoId;
    Long especialidadId;
    String descripcion;
    String direccion;
    LocalDateTime fechaDeseada;
    Integer duracionEstimada;
    EstadoSolicitud estado;
    LocalDateTime fechaCreacion;

    public static SolicitudResponse of(Solicitud solicitud) {
        return new SolicitudResponse(
                solicitud.getId(),
                solicitud.getClienteId(),
                solicitud.getTecnicoId(),
                solicitud.getEspecialidadId(),
                solicitud.getDescripcion(),
                solicitud.getDireccion(),
                solicitud.getFechaDeseada(),
                solicitud.getDuracionEstimada(),
                solicitud.getEstado(),
                solicitud.getFechaCreacion()
        );
    }
}
