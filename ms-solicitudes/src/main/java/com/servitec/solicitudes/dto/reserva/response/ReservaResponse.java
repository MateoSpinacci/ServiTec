package com.servitec.solicitudes.dto.reserva.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservaResponse {
    private Long id;
    private Long tecnicoId;
    private Long solicitudId;
}
