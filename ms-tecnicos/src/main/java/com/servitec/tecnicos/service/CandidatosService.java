package com.servitec.tecnicos.service;

import com.servitec.tecnicos.client.RutasClient;
import com.servitec.tecnicos.dto.tecnico.request.CandidatosRequest;
import com.servitec.tecnicos.dto.tecnico.response.CandidatoResponse;
import com.servitec.tecnicos.entity.Disponibilidad;
import com.servitec.tecnicos.entity.Tecnico;
import com.servitec.tecnicos.enums.EstadoReserva;
import com.servitec.tecnicos.repository.DisponibilidadRepository;
import com.servitec.tecnicos.repository.ReservaRepository;
import com.servitec.tecnicos.repository.TecnicoRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CandidatosService {

    TecnicoRepository tecnicoRepository;
    DisponibilidadRepository disponibilidadRepository;
    ReservaRepository reservaRepository;
    RutasClient rutasClient;

    public List<CandidatoResponse> buscarCandidatos(CandidatosRequest request) {

        LocalDateTime inicio = request.getFechaHoraSolicitada();
        LocalDateTime fin = inicio.plusMinutes(request.getDuracionEstimadaMin());

        List<Tecnico> tecnicos = tecnicoRepository.buscarActivosPorEspecialidad(request.getEspecialidadId());
        List<Tecnico> tecnicosDisponibles = new ArrayList<>();

        for (Tecnico tecnico : tecnicos) {
            List<Disponibilidad> disponibilidades =
                    disponibilidadRepository.findByTecnicoId(tecnico.getId());

            boolean tieneDisponibilidad = false;

            for (Disponibilidad disponibilidad : disponibilidades) {
                boolean cubreHorario =
                        !inicio.isBefore(disponibilidad.getInicio())
                                && !fin.isAfter(disponibilidad.getFin());
                if (cubreHorario) {
                    tieneDisponibilidad = true;
                    break;
                }
            }

            if (!tieneDisponibilidad) {
                continue;
            }

            boolean ocupado = reservaRepository.existeSuperposicion(tecnico.getId(), inicio, fin, EstadoReserva.ACTIVA);

            if (!ocupado) {
                tecnicosDisponibles.add(tecnico);
            }
        }

        List<CandidatoResponse> candidatos = new ArrayList<>();

        for (Tecnico tecnico : tecnicosDisponibles) {

            double segundos = rutasClient.calcularDuracionSegundos(
                    tecnico.getLatitudBase(),
                    tecnico.getLongitudBase(),
                    request.getLatitud(),
                    request.getLongitud()
            );

            if (segundos <= 45 * 60) {
                long minutos = (long) Math.ceil(segundos / 60.0);

                candidatos.add(new CandidatoResponse(
                        tecnico.getId(),
                        tecnico.getNombre(),
                        minutos
                ));
            }
        }

        return candidatos;
    }
}
