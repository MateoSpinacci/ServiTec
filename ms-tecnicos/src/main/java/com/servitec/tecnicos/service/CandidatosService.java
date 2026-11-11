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
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CandidatosService {

    static int MAX_TRASLADO_SEGUNDOS = 45 * 60;

    TecnicoRepository tecnicoRepository;
    DisponibilidadRepository disponibilidadRepository;
    ReservaRepository reservaRepository;
    RutasClient rutasClient;

    public List<CandidatoResponse> buscarCandidatos(CandidatosRequest request) {
        LocalDateTime inicio = request.getFechaHoraSolicitada();
        LocalDateTime fin = inicio.plusMinutes(request.getDuracionEstimadaMin());

        List<Tecnico> tecnicos = tecnicoRepository.buscarActivosPorEspecialidad(request.getEspecialidadId());

        List<CandidatoResponse> candidatos = new ArrayList<>();

        for (Tecnico tecnico : tecnicos) {
            List<Disponibilidad> disponibilidades = disponibilidadRepository.findByTecnicoId(tecnico.getId());

            boolean tieneDisponibilidad = disponibilidades.stream()
                    .anyMatch(d ->
                            !inicio.isBefore(d.getInicio())
                                    && !fin.isAfter(d.getFin())
                    );

            if (!tieneDisponibilidad) {
                continue;
            }

            boolean ocupado = reservaRepository.existeSuperposicion(
                    tecnico.getId(),
                    inicio,
                    fin,
                    EstadoReserva.ACTIVA
            );

            if (ocupado) {
                continue;
            }

            try {
                double segundos = calcularTiempoTraslado(
                        tecnico,
                        request.getLatitud(),
                        request.getLongitud()
                );

                if (segundos <= MAX_TRASLADO_SEGUNDOS) {
                    long minutos = (long) Math.ceil(segundos / 60.0);

                    candidatos.add(new CandidatoResponse(
                            tecnico.getId(),
                            tecnico.getNombre(),
                            minutos
                    ));
                }

            } catch (Exception ex) {
                log.warn(
                        "No se pudo calcular la ruta para el técnico {}",
                        tecnico.getId(),
                        ex
                );
            }
        }

        return candidatos;
    }

    public void validarTiempoTraslado(Long tecnicoId, Double latitudDestino, Double longitudDestino) {
        Tecnico tecnico = tecnicoRepository.findById(tecnicoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Técnico no encontrado"
                ));

        double segundos = calcularTiempoTraslado(
                tecnico,
                latitudDestino,
                longitudDestino
        );

        if (segundos > MAX_TRASLADO_SEGUNDOS) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El técnico supera el máximo de 45 minutos de traslado"
            );
        }
    }

    private double calcularTiempoTraslado(Tecnico tecnico, Double latitudDestino, Double longitudDestino) {
        double segundos = rutasClient.calcularDuracionSegundos(
                tecnico.getLatitudBase(),
                tecnico.getLongitudBase(),
                latitudDestino,
                longitudDestino
        );

        if (!Double.isFinite(segundos) || segundos < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "La API de rutas devolvió una duración inválida"
            );
        }

        return segundos;
    }
}