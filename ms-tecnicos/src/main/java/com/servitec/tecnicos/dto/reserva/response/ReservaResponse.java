package com.servitec.tecnicos.dto.reserva.response;

import com.servitec.tecnicos.entity.Reserva;
import com.servitec.tecnicos.enums.EstadoReserva;
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
public class ReservaResponse {
    Long id;
    Long tecnicoId;
    Long solicitudId;
    LocalDateTime inicio;
    LocalDateTime fin;
    EstadoReserva estado;

    public static ReservaResponse of(Reserva reserva) {
        return new ReservaResponse(
                reserva.getId(),
                reserva.getTecnico().getId(),
                reserva.getSolicitudId(),
                reserva.getInicio(),
                reserva.getFin(),
                reserva.getEstado()
        );
    }
}
