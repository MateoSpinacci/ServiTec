package com.servitec.solicitudes.dto.solicitud.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SolicitudRequest {

    @NotNull
    private Long especialidadId;

    @NotBlank
    private String descripcion;

    @NotBlank
    private String direccion;

    @NotNull
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    Double latitud;

    @NotNull
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    Double longitud;

    @NotNull
    @Future
    private LocalDateTime fechaDeseada;

    @NotNull
    @Positive
    private Integer duracionEstimada;
}