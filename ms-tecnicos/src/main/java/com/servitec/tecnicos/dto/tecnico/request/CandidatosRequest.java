package com.servitec.tecnicos.dto.tecnico.request;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CandidatosRequest {

    @NotNull
    Long especialidadId;

    @NotNull
    @Future
    LocalDateTime fechaHoraSolicitada;

    @NotNull
    @Positive
    Integer duracionEstimadaMin;

    @NotNull
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    Double latitud;

    @NotNull
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    Double longitud;
}
