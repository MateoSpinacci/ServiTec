package com.servitec.tecnicos.service;

import com.servitec.tecnicos.dto.disponibilidad.request.CrearDisponibilidadRequest;
import com.servitec.tecnicos.dto.disponibilidad.response.DisponibilidadResponse;
import com.servitec.tecnicos.dto.tecnico.response.TecnicoResponse;
import com.servitec.tecnicos.entity.Disponibilidad;
import com.servitec.tecnicos.entity.Tecnico;
import com.servitec.tecnicos.repository.DisponibilidadRepository;
import com.servitec.tecnicos.repository.TecnicoRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class DisponibilidadService {

    DisponibilidadRepository disponibilidadRepository;
    TecnicoRepository tecnicoRepository;

    public DisponibilidadResponse crear(Long id, CrearDisponibilidadRequest request) {
        Tecnico tecnico = tecnicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Técnico no encontrado."
                ));
        if (!tecnico.isActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El técnico no está activo."
            );
        }
        if (!request.getFin().isAfter(request.getInicio())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha de fin debe ser posterior al inicio"
            );
        }

        List<Disponibilidad> disponibilidades = disponibilidadRepository.findByTecnicoId(id);
        for (Disponibilidad disponibilidad : disponibilidades) {
            boolean seSuperponen =
                    request.getInicio().isBefore(disponibilidad.getFin())
                            && request.getFin().isAfter(disponibilidad.getInicio());

            if (seSuperponen) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La disponibilidad se superpone con otra existente."
                );
            }
        }

        Disponibilidad disponibilidad = new Disponibilidad(
                tecnico,
                request.getInicio(),
                request.getFin()
        );

        return DisponibilidadResponse.of(disponibilidadRepository.save(disponibilidad));
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
}
