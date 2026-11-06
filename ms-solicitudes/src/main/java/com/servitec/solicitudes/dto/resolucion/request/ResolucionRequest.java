package com.servitec.solicitudes.dto.resolucion.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResolucionRequest {

    @NotBlank
    private String diagnostico;

    @NotBlank
    private String resolucion;
}