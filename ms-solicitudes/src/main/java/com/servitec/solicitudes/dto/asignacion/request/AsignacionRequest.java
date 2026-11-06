package com.servitec.solicitudes.dto.asignacion.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AsignacionRequest {

    @NotNull
    @Positive
    private Long tecnicoId;
}
