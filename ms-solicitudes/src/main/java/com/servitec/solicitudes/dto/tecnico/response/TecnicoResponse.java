
package com.servitec.solicitudes.dto.tecnico.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TecnicoResponse {

    private Long id;
    private String nombre;
    private boolean activo;
    private List<EspecialidadResponse> especialidades;
}

