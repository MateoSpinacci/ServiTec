package com.servitec.tecnicos.dto.tecnico.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    Double latitud;

    @NotNull
    Double longitud;
}
