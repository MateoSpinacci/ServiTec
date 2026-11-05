package com.servitec.tecnicos.dto.reponse;

import com.servitec.tecnicos.entity.Especialidad;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EspecialidadResponse {
    Long id;
    String nombre;
    String descripcion;

    public static EspecialidadResponse of(Especialidad especialidad) {
        return new EspecialidadResponse(
                especialidad.getId(),
                especialidad.getNombre(),
                especialidad.getDescripcion()
        );
    }
}
