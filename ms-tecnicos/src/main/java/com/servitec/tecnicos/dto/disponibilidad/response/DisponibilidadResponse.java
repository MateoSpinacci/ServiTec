package com.servitec.tecnicos.dto.disponibilidad.response;


import com.servitec.tecnicos.entity.Disponibilidad;
import com.servitec.tecnicos.entity.Tecnico;
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
public class DisponibilidadResponse {
    Long id;
    Long tecnicoId;
    LocalDateTime inicio;
    LocalDateTime fin;

    public static DisponibilidadResponse of(Disponibilidad disponibilidad) {
        return new DisponibilidadResponse(
                disponibilidad.getId(),
                disponibilidad.getTecnico().getId(),
                disponibilidad.getInicio(),
                disponibilidad.getFin()
        );
    }
}
