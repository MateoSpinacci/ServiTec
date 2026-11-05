package com.servitec.tecnicos.dto.tecnico.response;

import com.servitec.tecnicos.dto.especialidad.response.EspecialidadResponse;
import com.servitec.tecnicos.entity.Tecnico;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TecnicoResponse {
    Long id;
    String keycloakUserId;
    String nombre;
    String telefono;
    String direccionBase;
    Double latitudBase;
    Double longitudBase;
    boolean activo;
    List<EspecialidadResponse> especialidades;

    public static TecnicoResponse of(Tecnico tecnico) {
        return new TecnicoResponse(
                tecnico.getId(),
                tecnico.getKeycloakUserId(),
                tecnico.getNombre(),
                tecnico.getTelefono(),
                tecnico.getDireccionBase(),
                tecnico.getLatitudBase(),
                tecnico.getLongitudBase(),
                tecnico.isActivo(),
                tecnico.getEspecialidades()
                        .stream()
                        .map(EspecialidadResponse::of)
                        .toList()
        );
    }
}
