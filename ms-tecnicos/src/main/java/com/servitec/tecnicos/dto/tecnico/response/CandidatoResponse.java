package com.servitec.tecnicos.dto.tecnico.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CandidatoResponse {

    Long tecnicoId;
    String nombre;
    Long tiempoTrasladoMinutos;
}