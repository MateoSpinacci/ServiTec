package com.servitec.tecnicos.service;


import com.servitec.tecnicos.dto.reserva.request.ReservaRequest;
import com.servitec.tecnicos.dto.reserva.response.ReservaResponse;
import com.servitec.tecnicos.dto.tecnico.response.TecnicoResponse;
import com.servitec.tecnicos.entity.Disponibilidad;
import com.servitec.tecnicos.entity.Reserva;
import com.servitec.tecnicos.entity.Tecnico;
import com.servitec.tecnicos.enums.EstadoReserva;
import com.servitec.tecnicos.repository.DisponibilidadRepository;
import com.servitec.tecnicos.repository.ReservaRepository;
import com.servitec.tecnicos.repository.TecnicoRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class ReservaService {

    ReservaRepository reservaRepository;
    TecnicoRepository tecnicoRepository;
    DisponibilidadRepository disponibilidadRepository;

    @Transactional
    public ReservaResponse crear(ReservaRequest reservaRequest) {
        if (reservaRequest.getInicio() == null || reservaRequest.getFin() == null
                || !reservaRequest.getInicio().isBefore(reservaRequest.getFin())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El intervalo de la reserva es inválido"
            );
        }

        Tecnico tecnico = tecnicoRepository.findByIdForUpdate(reservaRequest.getTecnicoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Técnico no encontrado"
                ));
        if (!tecnico.isActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El técnico está inactivo"
            );
        }

        List<Disponibilidad> disponibilidades =
                disponibilidadRepository.findByTecnicoId(reservaRequest.getTecnicoId());

        boolean estaDisponible = disponibilidades.stream()
                .anyMatch(d ->
                        !reservaRequest.getInicio().isBefore(d.getInicio())
                                && !reservaRequest.getFin().isAfter(d.getFin())
                );
        if (!estaDisponible) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El técnico no tiene disponibilidad suficiente"
            );
        }

        boolean hayConflicto = reservaRepository.existeSuperposicion(
                reservaRequest.getTecnicoId(),
                reservaRequest.getInicio(),
                reservaRequest.getFin(),
                EstadoReserva.ACTIVA
        );
        if (hayConflicto) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El técnico ya tiene una reserva en ese horario"
            );
        }

        boolean yaReservada = reservaRepository.existsBySolicitudIdAndEstado(
                reservaRequest.getSolicitudId(),
                EstadoReserva.ACTIVA
        );
        if (yaReservada) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La solicitud ya tiene una reserva activa"
            );
        }

        Reserva reserva = new Reserva();
        reserva.setTecnico(tecnico);
        reserva.setSolicitudId(reservaRequest.getSolicitudId());
        reserva.setInicio(reservaRequest.getInicio());
        reserva.setFin(reservaRequest.getFin());
        reserva.setEstado(EstadoReserva.ACTIVA);

        return ReservaResponse.of(reservaRepository.save(reserva));
    }

    @Transactional
    public void liberarReserva(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reserva no encontrada."));

        if (reserva.getEstado() == EstadoReserva.LIBERADA) {
            return;
        }

        reserva.setEstado(EstadoReserva.LIBERADA);
        reservaRepository.save(reserva);
    }
}

