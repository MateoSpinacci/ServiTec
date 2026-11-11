package com.servitec.solicitudes.dto.asignacion.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ValidarTrasladoRequest {
    @NotNull
    Long tecnicoId;

    @NotNull
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    Double latitud;

    @NotNull
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    Double longitud;
}
