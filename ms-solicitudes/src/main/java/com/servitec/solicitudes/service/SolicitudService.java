package com.servitec.solicitudes.service;

import com.servitec.solicitudes.client.TecnicosClient;
import com.servitec.solicitudes.dto.asignacion.request.AsignacionRequest;
import com.servitec.solicitudes.dto.reserva.request.ReservaRequest;
import com.servitec.solicitudes.dto.reserva.response.ReservaResponse;
import com.servitec.solicitudes.dto.resolucion.request.ResolucionRequest;
import com.servitec.solicitudes.dto.solicitud.request.SolicitudRequest;
import com.servitec.solicitudes.dto.tecnico.response.TecnicoResponse;
import com.servitec.solicitudes.entity.Asignacion;
import com.servitec.solicitudes.entity.Intervencion;
import com.servitec.solicitudes.entity.Solicitud;
import com.servitec.solicitudes.enums.EstadoSolicitud;
import com.servitec.solicitudes.repository.AsignacionRepository;
import com.servitec.solicitudes.repository.IntervencionRepository;
import com.servitec.solicitudes.repository.SolicitudRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final AsignacionRepository asignacionRepository;
    private final IntervencionRepository intervencionRepository;
    private final TecnicosClient tecnicosClient;

    public SolicitudService(
            SolicitudRepository solicitudRepository,
            AsignacionRepository asignacionRepository,
            IntervencionRepository intervencionRepository,
            TecnicosClient tecnicosClient) {

        this.solicitudRepository = solicitudRepository;
        this.asignacionRepository = asignacionRepository;
        this.intervencionRepository = intervencionRepository;
        this.tecnicosClient = tecnicosClient;
    }

    public List<Solicitud> obtenerTodas() {
        return solicitudRepository.findAll();
    }

    public Optional<Solicitud> obtenerPorId(Long id) {
        return solicitudRepository.findById(id);
    }

    public Long obtenerTecnicoAsignado(Long solicitudId) {
        return asignacionRepository.findBySolicitudId(solicitudId)
                .map(Asignacion::getTecnicoId)
                .orElse(null);
    }

    @Transactional
    public Solicitud crear(SolicitudRequest request, Long clienteId) {

        Solicitud solicitud = new Solicitud();

        solicitud.setClienteId(clienteId);
        solicitud.setEspecialidadId(request.getEspecialidadId());
        solicitud.setDescripcion(request.getDescripcion());
        solicitud.setDireccion(request.getDireccion());
        solicitud.setLatitud(request.getLatitud());
        solicitud.setLongitud(request.getLongitud());
        solicitud.setFechaSolicitud(request.getFechaDeseada());
        solicitud.setDuracionEstimada(request.getDuracionEstimada());

        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setFechaCreacion(LocalDateTime.now());

        return solicitudRepository.save(solicitud);
    }

    @Transactional
    public Solicitud cancelar(Long id, Long clienteId) {

        Solicitud solicitud = buscarSolicitud(id);

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

    @Transactional
    public Solicitud asignarTecnico(Long id, AsignacionRequest request) {

        Solicitud solicitud = buscarSolicitud(id);

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se pueden asignar solicitudes pendientes"
            );
        }

        if (asignacionRepository.existsBySolicitudId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La solicitud ya tiene una asignación"
            );
        }

        Long tecnicoId = request.getTecnicoId();

        TecnicoResponse tecnico = tecnicosClient.obtenerPorId(tecnicoId);

        if (tecnico == null || !tecnico.isActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El técnico no está activo"
            );
        }

        boolean tieneEspecialidad = tecnico.getEspecialidades() != null
                && tecnico.getEspecialidades().stream()
                .anyMatch(e ->
                        e.getId().equals(solicitud.getEspecialidadId())
                );

        if (!tieneEspecialidad) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El técnico no tiene la especialidad requerida"
            );
        }

        LocalDateTime inicio = solicitud.getFechaSolicitud();
        LocalDateTime fin = inicio.plusMinutes(
                solicitud.getDuracionEstimada()
        );

        ReservaRequest reservaRequest = new ReservaRequest(
                tecnicoId,
                solicitud.getId(),
                inicio,
                fin
        );

        // ms-tecnicos revalida disponibilidad y superposiciones.
        ReservaResponse reserva = tecnicosClient.crearReserva(reservaRequest);

        Asignacion asignacion = new Asignacion();
        asignacion.setSolicitud(solicitud);
        asignacion.setTecnicoId(tecnicoId);
        asignacion.setReservaId(reserva.getId());
        asignacion.setFechaCreacion(LocalDateTime.now());

        try {
            asignacionRepository.saveAndFlush(asignacion);

            solicitud.setEstado(EstadoSolicitud.ASIGNADA);
            return solicitudRepository.saveAndFlush(solicitud);

        } catch (RuntimeException e) {
            try {
                tecnicosClient.liberarReserva(reserva.getId());
            } catch (RuntimeException compensacionError) {
                e.addSuppressed(compensacionError);
            }
            throw e;
        }
    }

    @Transactional
    public Solicitud iniciarTrabajo(Long id, Long tecnicoId) {

        Solicitud solicitud = buscarSolicitud(id);
        Asignacion asignacion = buscarAsignacion(id);

        validarTecnicoAsignado(asignacion, tecnicoId);

        if (solicitud.getEstado() != EstadoSolicitud.ASIGNADA) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se pueden iniciar trabajos asignados"
            );
        }

        Intervencion intervencion = new Intervencion();
        intervencion.setSolicitud(solicitud);
        intervencion.setAsignacion(asignacion);
        intervencion.setInicio(LocalDateTime.now());

        intervencionRepository.save(intervencion);

        solicitud.setEstado(EstadoSolicitud.EN_CURSO);
        return solicitudRepository.save(solicitud);
    }

    @Transactional
    public Solicitud resolverTrabajo(
            Long id,
            Long tecnicoId,
            ResolucionRequest request) {

        Solicitud solicitud = buscarSolicitud(id);
        Asignacion asignacion = buscarAsignacion(id);

        validarTecnicoAsignado(asignacion, tecnicoId);

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
        intervencion.setFin(LocalDateTime.now());

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

    private Solicitud buscarSolicitud(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Solicitud no encontrada"
                ));
    }

    private Asignacion buscarAsignacion(Long solicitudId) {
        return asignacionRepository.findBySolicitudId(solicitudId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "La solicitud no tiene una asignación"
                ));
    }

    private void validarTecnicoAsignado(
            Asignacion asignacion,
            Long tecnicoId) {

        if (!asignacion.getTecnicoId().equals(tecnicoId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El técnico no está asignado a esta solicitud"
            );
        }
    }
}