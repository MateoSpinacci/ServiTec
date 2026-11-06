
package com.servitec.solicitudes.dto.disponibilidad.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class DisponibilidadResponse {

    private Long id;
    private Long tecnicoId;
    private LocalDateTime inicio;
    private LocalDateTime fin;
}

