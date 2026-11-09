package com.servitec.solicitudes.dto.reserva.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReservaRequest {

    private Long tecnicoId;
    private Long solicitudId;
    private LocalDateTime inicio;
    private LocalDateTime fin;
}