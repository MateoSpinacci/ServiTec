package com.servitec.tecnicos.service;

import com.servitec.tecnicos.dto.disponibilidad.request.DisponibilidadRequest;
import com.servitec.tecnicos.dto.disponibilidad.response.DisponibilidadResponse;
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
public class DisponibilidadService {

    DisponibilidadRepository disponibilidadRepository;
    TecnicoRepository tecnicoRepository;
    ReservaRepository reservaRepository;

    @Transactional
    public DisponibilidadResponse crear(Long id, DisponibilidadRequest request) {

        validarIntervalo(request);

        Tecnico tecnico = tecnicoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Técnico no encontrado."
                ));

        if (!tecnico.isActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El técnico no está activo."
            );
        }

        List<Disponibilidad> disponibilidades =
                disponibilidadRepository.findByTecnicoId(id);

        validarSuperposiciones(
                disponibilidades,
                null,
                request.getInicio(),
                request.getFin()
        );

        Disponibilidad disponibilidad = new Disponibilidad(
                tecnico,
                request.getInicio(),
                request.getFin()
        );

        return DisponibilidadResponse.of(
                disponibilidadRepository.save(disponibilidad)
        );
    }

    public List<DisponibilidadResponse> listar(Long id) {

        if (!tecnicoRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Técnico no encontrado."
            );
        }

        return disponibilidadRepository.findByTecnicoId(id)
                .stream()
                .map(DisponibilidadResponse::of)
                .toList();
    }

    @Transactional
    public DisponibilidadResponse actualizarDisponibilidad(
            Long tecnicoId,
            Long disponibilidadId,
            DisponibilidadRequest request) {

        validarIntervalo(request);

        Tecnico tecnico = tecnicoRepository.findByIdForUpdate(tecnicoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Técnico no encontrado."
                ));

        if (!tecnico.isActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El técnico no está activo."
            );
        }

        Disponibilidad disponibilidad = disponibilidadRepository
                .findById(disponibilidadId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Disponibilidad no encontrada."
                ));

        if (!disponibilidad.getTecnico().getId().equals(tecnicoId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "La disponibilidad no pertenece al técnico indicado."
            );
        }

        List<Disponibilidad> disponibilidades =
                disponibilidadRepository.findByTecnicoId(tecnicoId);

        validarSuperposiciones(
                disponibilidades,
                disponibilidadId,
                request.getInicio(),
                request.getFin()
        );

        List<Reserva> reservasActivas =
                reservaRepository.findByTecnicoIdAndEstado(
                        tecnicoId,
                        EstadoReserva.ACTIVA
                );

        for (Reserva reserva : reservasActivas) {
            boolean sigueCubierta = disponibilidades.stream()
                    .anyMatch(d -> {
                        LocalDateTime inicio = d.getId().equals(disponibilidadId)
                                ? request.getInicio()
                                : d.getInicio();
                        LocalDateTime fin = d.getId().equals(disponibilidadId)
                                ? request.getFin()
                                : d.getFin();

                        return !reserva.getInicio().isBefore(inicio)
                                && !reserva.getFin().isAfter(fin);
                    });

            if (!sigueCubierta) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "La modificación dejaría una reserva activa fuera de la disponibilidad."
                );
            }
        }

        disponibilidad.setInicio(request.getInicio());
        disponibilidad.setFin(request.getFin());

        return DisponibilidadResponse.of(disponibilidadRepository.save(disponibilidad));
    }

    private void validarIntervalo(DisponibilidadRequest request) {

        if (request.getInicio() == null || request.getFin() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Las fechas de inicio y fin son obligatorias."
            );
        }
        if (!request.getFin().isAfter(request.getInicio())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha de fin debe ser posterior al inicio."
            );
        }
    }

    private void validarSuperposiciones(
            List<Disponibilidad> disponibilidades,
            Long disponibilidadIdExcluida,
            LocalDateTime inicio,
            LocalDateTime fin) {

        for (Disponibilidad disponibilidad : disponibilidades) {
            if (disponibilidad.getId().equals(disponibilidadIdExcluida)) {
                continue;
            }

            boolean seSuperponen = inicio.isBefore(disponibilidad.getFin()) && fin.isAfter(disponibilidad.getInicio());

            if (seSuperponen) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "La disponibilidad se superpone con otra existente."
                );
            }
        }
    }
}
