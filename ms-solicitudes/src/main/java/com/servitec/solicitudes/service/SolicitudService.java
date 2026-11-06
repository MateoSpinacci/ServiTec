package com.servitec.solicitudes.service;

import com.servitec.solicitudes.entity.Solicitud;
import com.servitec.solicitudes.repository.SolicitudRepository;
import com.servitec.solicitudes.dto.solicitud.request.SolicitudRequest;
import com.servitec.solicitudes.dto.asignacion.request.AsignacionRequest;
import com.servitec.solicitudes.dto.resolucion.request.ResolucionRequest;
import com.servitec.solicitudes.enums.EstadoSolicitud;
import com.servitec.solicitudes.repository.IntervencionRepository;
import com.servitec.solicitudes.entity.Intervencion;
import com.servitec.solicitudes.client.TecnicosClient;
import com.servitec.solicitudes.dto.tecnico.response.TecnicoResponse;
import com.servitec.solicitudes.dto.disponibilidad.response.DisponibilidadResponse;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final IntervencionRepository intervencionRepository;
    private final TecnicosClient tecnicosClient;

    public SolicitudService(
            SolicitudRepository solicitudRepository,
            IntervencionRepository intervencionRepository,
            TecnicosClient tecnicosClient) {

        this.solicitudRepository = solicitudRepository;
        this.intervencionRepository = intervencionRepository;
        this.tecnicosClient = tecnicosClient;
    }

    public List<Solicitud> obtenerTodas() {
        return solicitudRepository.findAll();
    }

    public Optional<Solicitud> obtenerPorId(Long id) {
        return solicitudRepository.findById(id);
    }

    public Solicitud crear(SolicitudRequest request, Long clienteId) {

        Solicitud solicitud = new Solicitud();

        solicitud.setClienteId(clienteId);
        solicitud.setEspecialidadId(request.getEspecialidadId());
        solicitud.setDescripcion(request.getDescripcion());
        solicitud.setDireccion(request.getDireccion());
        solicitud.setFechaDeseada(request.getFechaDeseada());
        solicitud.setDuracionEstimada(request.getDuracionEstimada());

        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setFechaCreacion(LocalDateTime.now());

        return solicitudRepository.save(solicitud);
    }

    public Solicitud cancelar(Long id, Long clienteId) {

        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Solicitud no encontrada"
                ));

        if (!solicitud.getClienteId().equals(clienteId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tenés permiso para cancelar esta solicitud"
            );
        }

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se pueden cancelar solicitudes pendientes"
            );
        }

        solicitud.setEstado(EstadoSolicitud.CANCELADA);

        return solicitudRepository.save(solicitud);
    }


    public Solicitud asignarTecnico(Long id, AsignacionRequest request) {

        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se pueden asignar solicitudes pendientes");
        }

        TecnicoResponse tecnico = tecnicosClient.obtenerPorId(request.getTecnicoId());

        if (!tecnico.isActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El técnico no está activo");
        }

        boolean tieneEspecialidad = tecnico.getEspecialidades()
                .stream()
                .anyMatch(especialidad ->
                        especialidad.getId().equals(solicitud.getEspecialidadId()));

        if (!tieneEspecialidad) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El técnico no tiene la especialidad requerida");
        }

        List<DisponibilidadResponse> disponibilidades =
                tecnicosClient.obtenerDisponibilidades(request.getTecnicoId());

        LocalDateTime inicioTrabajo = solicitud.getFechaDeseada();
        LocalDateTime finTrabajo = inicioTrabajo.plusMinutes(
                solicitud.getDuracionEstimada()
        );

        boolean estaDisponible = disponibilidades.stream()
                .anyMatch(disponibilidad ->
                        !inicioTrabajo.isBefore(disponibilidad.getInicio())
                                && !finTrabajo.isAfter(disponibilidad.getFin())
                );

        if (!estaDisponible) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El técnico no tiene disponibilidad para el horario solicitado"
            );
        }

        solicitud.setTecnicoId(request.getTecnicoId());
        solicitud.setEstado(EstadoSolicitud.ASIGNADA);

        return solicitudRepository.save(solicitud);
    }


    @Transactional
    public Solicitud iniciarTrabajo(Long id, Long tecnicoId) {

        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Solicitud no encontrada"
                ));

        if (solicitud.getTecnicoId() == null ||
                !solicitud.getTecnicoId().equals(tecnicoId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tenés permiso para iniciar este trabajo"
            );
        }

        if (solicitud.getEstado() != EstadoSolicitud.ASIGNADA) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se pueden iniciar trabajos asignados"
            );
        }

        Intervencion intervencion = new Intervencion();

        intervencion.setSolicitudId(solicitud.getId());
        intervencion.setTecnicoId(tecnicoId);
        intervencion.setFechaInicio(LocalDateTime.now());

        intervencionRepository.save(intervencion);

        solicitud.setEstado(EstadoSolicitud.EN_CURSO);

        return solicitudRepository.save(solicitud);
    }

    @Transactional
    public Solicitud resolverTrabajo(Long id, Long tecnicoId, ResolucionRequest request) {

        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Solicitud no encontrada"
                ));

        if (solicitud.getTecnicoId() == null ||
                !solicitud.getTecnicoId().equals(tecnicoId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tenés permiso para resolver este trabajo"
            );
        }

        if (solicitud.getEstado() != EstadoSolicitud.EN_CURSO) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se pueden resolver trabajos en curso"
            );
        }

        Intervencion intervencion = intervencionRepository
                .findBySolicitudId(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "No se encontró la intervención de esta solicitud"
                ));

        intervencion.setDiagnostico(request.getDiagnostico());
        intervencion.setResolucion(request.getResolucion());
        intervencion.setFechaResolucion(LocalDateTime.now());

        intervencionRepository.save(intervencion);

        solicitud.setEstado(EstadoSolicitud.RESUELTA);

        return solicitudRepository.save(solicitud);
    }

    public List<Solicitud> obtenerTrabajosDelTecnico(Long tecnicoId) {

        return solicitudRepository.findByTecnicoIdAndEstadoIn(
                tecnicoId,
                List.of(
                        EstadoSolicitud.ASIGNADA,
                        EstadoSolicitud.EN_CURSO
                )
        );
    }
}

