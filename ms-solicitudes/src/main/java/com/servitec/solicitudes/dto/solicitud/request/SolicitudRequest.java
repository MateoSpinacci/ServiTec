package com.servitec.solicitudes.dto.solicitud.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    @Future
    private LocalDateTime fechaDeseada;

    @NotNull
    @Positive
    private Integer duracionEstimada;
}