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
    String clienteId;
    Long tecnicoId;
    Long especialidadId;
    String descripcion;
    String direccion;
    Double latitud;
    Double longitud;
    LocalDateTime fechaDeseada;
    Integer duracionEstimada;
    EstadoSolicitud estado;
    LocalDateTime fechaCreacion;

    public static SolicitudResponse of(Solicitud solicitud, Long tecnicoId) {
        return new SolicitudResponse(
                solicitud.getId(),
                solicitud.getClienteId(),
                tecnicoId,
                solicitud.getEspecialidadId(),
                solicitud.getDescripcion(),
                solicitud.getDireccion(),
                solicitud.getLatitud(),
                solicitud.getLongitud(),
                solicitud.getFechaSolicitud(),
                solicitud.getDuracionEstimada(),
                solicitud.getEstado(),
                solicitud.getFechaCreacion()
        );
    }
}