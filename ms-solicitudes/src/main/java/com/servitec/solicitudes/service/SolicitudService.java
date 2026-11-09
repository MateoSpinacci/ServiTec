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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SolicitudService {
    SolicitudRepository solicitudRepository;
    AsignacionRepository asignacionRepository;
    IntervencionRepository intervencionRepository;
    TecnicosClient tecnicosClient;

    public List<Solicitud> obtenerTodas(String keycloakUserId,
                                        Collection<? extends GrantedAuthority> authorities,
                                        EstadoSolicitud estado) {

        boolean esAdmin = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (esAdmin) {
            if (estado != null) {
                return solicitudRepository.findByEstado(estado);
            }

            return solicitudRepository.findAll();
        }

        if (estado != null) {
            return solicitudRepository.findByClienteIdAndEstado(
                    keycloakUserId,
                    estado
            );
        }

        return solicitudRepository.findByClienteId(keycloakUserId);
    }

    public Solicitud obtenerPorId(Long id, String keycloakUserId, Collection<? extends GrantedAuthority> authorities) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Solicitud no encontrada"
                ));

        boolean esAdmin = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean esCliente = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));

        boolean esTecnico = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TECNICO"));

        if (esAdmin) {
            return solicitud;
        }

        if (esCliente && solicitud.getClienteId().equals(keycloakUserId)) {
            return solicitud;
        }

        if (esTecnico) {
            Asignacion asignacion = asignacionRepository
                    .findBySolicitudId(id)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "No estás asignado a esta solicitud"
                    ));

            validarTecnicoAsignado(asignacion, keycloakUserId);

            return solicitud;
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "No tenés permiso para consultar esta solicitud"
        );
    }

    public Long obtenerTecnicoAsignado(Long solicitudId) {
        return asignacionRepository.findBySolicitudId(solicitudId)
                .map(Asignacion::getTecnicoId)
                .orElse(null);
    }

    @Transactional
    public Solicitud crear(SolicitudRequest request, String clienteId) {

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
    public Solicitud cancelar(Long id, String clienteId) {

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

        validarSolicitudAsignable(solicitud);

        Long tecnicoId = request.getTecnicoId();

        TecnicoResponse tecnico = tecnicosClient.obtenerPorId(tecnicoId);

        validarTecnico(tecnico, solicitud);

        tecnicosClient.validarTiempoTraslado(tecnicoId, solicitud.getLatitud(), solicitud.getLongitud());

        ReservaRequest reservaRequest = construirReservaRequest(tecnicoId, solicitud);

        ReservaResponse reserva = tecnicosClient.crearReserva(reservaRequest);

        try {
            Asignacion asignacion = new Asignacion();
            asignacion.setSolicitud(solicitud);
            asignacion.setTecnicoId(tecnicoId);
            asignacion.setReservaId(reserva.getId());
            asignacion.setFechaCreacion(LocalDateTime.now());

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
    public Solicitud iniciarTrabajo(Long id, String tecnicoId) {

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
            String tecnicoId,
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

    public List<Solicitud> obtenerTrabajosDelTecnico(String keycloakUserId) {
        TecnicoResponse tecnico = tecnicosClient.obtenerPorKeycloakUserId(keycloakUserId);

        return solicitudRepository.findTrabajosDelTecnico(
                tecnico.getId(),
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
            String keycloakUserId) {

        Long tecnicoId = asignacion.getTecnicoId();

        TecnicoResponse tecnico = tecnicosClient.obtenerPorId(tecnicoId);

        if (!tecnico.getKeycloakUserId().equals(keycloakUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El técnico no está asignado a esta solicitud"
            );
        }
    }

    private void validarSolicitudAsignable(Solicitud solicitud) {

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se pueden asignar solicitudes pendientes"
            );
        }

        if (asignacionRepository.existsBySolicitudId(solicitud.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La solicitud ya tiene una asignación"
            );
        }
    }

    private void validarTecnico(
            TecnicoResponse tecnico,
            Solicitud solicitud) {

        if (tecnico == null || !tecnico.isActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El técnico no está activo"
            );
        }

        boolean tieneEspecialidad = tecnico.getEspecialidades() != null
                && tecnico.getEspecialidades().stream()
                .anyMatch(e -> Objects.equals(
                        e.getId(),
                        solicitud.getEspecialidadId()
                ));

        if (!tieneEspecialidad) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El técnico no tiene la especialidad requerida"
            );
        }
    }

    private ReservaRequest construirReservaRequest(
            Long tecnicoId,
            Solicitud solicitud) {

        LocalDateTime inicio = solicitud.getFechaSolicitud();

        LocalDateTime fin = inicio.plusMinutes(
                solicitud.getDuracionEstimada()
        );

        return new ReservaRequest(
                tecnicoId,
                solicitud.getId(),
                inicio,
                fin
        );
    }
}