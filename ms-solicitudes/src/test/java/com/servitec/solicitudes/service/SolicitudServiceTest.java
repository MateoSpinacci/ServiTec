package com.servitec.solicitudes.service;

import com.servitec.solicitudes.client.TecnicosClient;
import com.servitec.solicitudes.dto.asignacion.request.AsignacionRequest;
import com.servitec.solicitudes.dto.reserva.request.ReservaRequest;
import com.servitec.solicitudes.dto.reserva.response.ReservaResponse;
import com.servitec.solicitudes.dto.resolucion.request.ResolucionRequest;
import com.servitec.solicitudes.dto.solicitud.request.SolicitudRequest;
import com.servitec.solicitudes.dto.tecnico.response.EspecialidadResponse;
import com.servitec.solicitudes.dto.tecnico.response.TecnicoResponse;
import com.servitec.solicitudes.entity.Asignacion;
import com.servitec.solicitudes.entity.Intervencion;
import com.servitec.solicitudes.entity.Solicitud;
import com.servitec.solicitudes.enums.EstadoSolicitud;
import com.servitec.solicitudes.repository.AsignacionRepository;
import com.servitec.solicitudes.repository.IntervencionRepository;
import com.servitec.solicitudes.repository.SolicitudRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudServiceTest {

    private static final Long SOLICITUD_ID = 10L;
    private static final Long TECNICO_ID = 20L;
    private static final Long ESPECIALIDAD_ID = 3L;
    private static final Long RESERVA_ID = 30L;

    private static final String CLIENTE_ID = "cliente-keycloak-123";
    private static final String OTRO_CLIENTE_ID = "otro-cliente-keycloak";

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private AsignacionRepository asignacionRepository;

    @Mock
    private IntervencionRepository intervencionRepository;

    @Mock
    private TecnicosClient tecnicosClient;

    @InjectMocks
    private SolicitudService solicitudService;

    @Test
    void crearGuardaSolicitudPendienteConDatosDelRequest() {
        LocalDateTime fechaDeseada = LocalDateTime.now().plusDays(1);

        SolicitudRequest request = new SolicitudRequest();
        request.setEspecialidadId(ESPECIALIDAD_ID);
        request.setDescripcion("Reparar una canilla");
        request.setDireccion("Calle 123");
        request.setLatitud(-34.6);
        request.setLongitud(-58.4);
        request.setFechaDeseada(fechaDeseada);
        request.setDuracionEstimada(60);

        when(solicitudRepository.save(any(Solicitud.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Solicitud creada = solicitudService.crear(request, CLIENTE_ID);

        assertAll(
                () -> assertEquals(CLIENTE_ID, creada.getClienteId()),
                () -> assertEquals(ESPECIALIDAD_ID, creada.getEspecialidadId()),
                () -> assertEquals("Reparar una canilla", creada.getDescripcion()),
                () -> assertEquals("Calle 123", creada.getDireccion()),
                () -> assertEquals(-34.6, creada.getLatitud()),
                () -> assertEquals(-58.4, creada.getLongitud()),
                () -> assertEquals(fechaDeseada, creada.getFechaSolicitud()),
                () -> assertEquals(60, creada.getDuracionEstimada()),
                () -> assertEquals(EstadoSolicitud.PENDIENTE, creada.getEstado()),
                () -> assertNotNull(creada.getFechaCreacion())
        );

        verify(solicitudRepository).save(creada);
    }

    @Test
    void cancelarSolicitudPendienteDelPropietario() {
        Solicitud solicitud = crearSolicitud(
                EstadoSolicitud.PENDIENTE, CLIENTE_ID
        );

        when(solicitudRepository.findById(SOLICITUD_ID))
                .thenReturn(Optional.of(solicitud));

        when(solicitudRepository.save(solicitud))
                .thenReturn(solicitud);

        Solicitud cancelada =
                solicitudService.cancelar(SOLICITUD_ID, CLIENTE_ID);

        assertEquals(EstadoSolicitud.CANCELADA, cancelada.getEstado());
        verify(solicitudRepository).save(solicitud);
    }

    @Test
    void cancelarSolicitudAjenaDevuelveForbidden() {
        Solicitud solicitud = crearSolicitud(
                EstadoSolicitud.PENDIENTE, OTRO_CLIENTE_ID
        );

        when(solicitudRepository.findById(SOLICITUD_ID))
                .thenReturn(Optional.of(solicitud));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> solicitudService.cancelar(SOLICITUD_ID, CLIENTE_ID)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());

        verify(solicitudRepository, never())
                .save(any(Solicitud.class));
    }

    @Test
    void cancelarSolicitudInexistenteDevuelveNotFound() {
        when(solicitudRepository.findById(SOLICITUD_ID))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> solicitudService.cancelar(SOLICITUD_ID, CLIENTE_ID)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(solicitudRepository, never())
                .save(any(Solicitud.class));
    }

    @Test
    void asignarTecnicoCreaReservaYActualizaEstado() {
        Solicitud solicitud = crearSolicitud(
                EstadoSolicitud.PENDIENTE, CLIENTE_ID
        );

        LocalDateTime inicio = LocalDateTime.now().plusDays(1);

        solicitud.setEspecialidadId(ESPECIALIDAD_ID);
        solicitud.setFechaSolicitud(inicio);
        solicitud.setDuracionEstimada(60);

        AsignacionRequest request = new AsignacionRequest();
        request.setTecnicoId(TECNICO_ID);

        TecnicoResponse tecnico = mock(TecnicoResponse.class);
        EspecialidadResponse especialidad =
                mock(EspecialidadResponse.class);
        ReservaResponse reserva = mock(ReservaResponse.class);

        when(solicitudRepository.findById(SOLICITUD_ID))
                .thenReturn(Optional.of(solicitud));

        when(tecnicosClient.obtenerPorId(TECNICO_ID))
                .thenReturn(tecnico);

        when(tecnico.isActivo()).thenReturn(true);

        when(tecnico.getEspecialidades())
                .thenReturn(List.of(especialidad));

        when(especialidad.getId())
                .thenReturn(ESPECIALIDAD_ID);

        when(tecnicosClient.crearReserva(any(ReservaRequest.class)))
                .thenReturn(reserva);

        when(reserva.getId()).thenReturn(RESERVA_ID);

        when(asignacionRepository.saveAndFlush(any(Asignacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(solicitudRepository.saveAndFlush(solicitud))
                .thenReturn(solicitud);

        Solicitud asignada =
                solicitudService.asignarTecnico(SOLICITUD_ID, request);

        assertEquals(EstadoSolicitud.ASIGNADA, asignada.getEstado());

        ArgumentCaptor<ReservaRequest> reservaCaptor =
                ArgumentCaptor.forClass(ReservaRequest.class);

        verify(tecnicosClient).crearReserva(reservaCaptor.capture());

        assertNotNull(reservaCaptor.getValue());

        ArgumentCaptor<Asignacion> asignacionCaptor =
                ArgumentCaptor.forClass(Asignacion.class);

        verify(asignacionRepository)
                .saveAndFlush(asignacionCaptor.capture());

        Asignacion asignacionGuardada = asignacionCaptor.getValue();

        assertAll(
                () -> assertSame(
                        solicitud, asignacionGuardada.getSolicitud()
                ),
                () -> assertEquals(
                        TECNICO_ID, asignacionGuardada.getTecnicoId()
                ),
                () -> assertEquals(
                        RESERVA_ID, asignacionGuardada.getReservaId()
                )
        );

        verify(solicitudRepository).saveAndFlush(solicitud);
    }

    @Test
    void iniciarTrabajoCreaIntervencionYCambiaEstado() {
        Solicitud solicitud = crearSolicitud(
                EstadoSolicitud.ASIGNADA, CLIENTE_ID
        );

        Asignacion asignacion = crearAsignacion(solicitud, TECNICO_ID);

        when(solicitudRepository.findById(SOLICITUD_ID))
                .thenReturn(Optional.of(solicitud));

        when(asignacionRepository.findBySolicitudId(SOLICITUD_ID))
                .thenReturn(Optional.of(asignacion));

        when(solicitudRepository.save(solicitud))
                .thenReturn(solicitud);

        Solicitud enCurso =
                solicitudService.iniciarTrabajo(SOLICITUD_ID, TECNICO_ID);

        assertEquals(EstadoSolicitud.EN_CURSO, enCurso.getEstado());

        ArgumentCaptor<Intervencion> captor =
                ArgumentCaptor.forClass(Intervencion.class);

        verify(intervencionRepository).save(captor.capture());

        Intervencion intervencion = captor.getValue();

        assertAll(
                () -> assertSame(solicitud, intervencion.getSolicitud()),
                () -> assertSame(asignacion, intervencion.getAsignacion()),
                () -> assertNotNull(intervencion.getInicio())
        );

        verify(solicitudRepository).save(solicitud);
    }

    @Test
    void resolverTrabajoGuardaDiagnosticoYResolucion() {
        Solicitud solicitud = crearSolicitud(
                EstadoSolicitud.EN_CURSO, CLIENTE_ID
        );

        Asignacion asignacion = crearAsignacion(solicitud, TECNICO_ID);
        Intervencion intervencion = new Intervencion();

        ResolucionRequest request = new ResolucionRequest();
        request.setDiagnostico("Pérdida en la conexión");
        request.setResolucion("Se reemplazó el flexible");

        when(solicitudRepository.findById(SOLICITUD_ID))
                .thenReturn(Optional.of(solicitud));

        when(asignacionRepository.findBySolicitudId(SOLICITUD_ID))
                .thenReturn(Optional.of(asignacion));

        when(intervencionRepository.findBySolicitudId(SOLICITUD_ID))
                .thenReturn(Optional.of(intervencion));

        when(solicitudRepository.save(solicitud))
                .thenReturn(solicitud);

        Solicitud resuelta = solicitudService.resolverTrabajo(
                SOLICITUD_ID, TECNICO_ID, request
        );

        assertAll(
                () -> assertEquals(
                        EstadoSolicitud.RESUELTA, resuelta.getEstado()
                ),
                () -> assertEquals(
                        "Pérdida en la conexión",
                        intervencion.getDiagnostico()
                ),
                () -> assertEquals(
                        "Se reemplazó el flexible",
                        intervencion.getResolucion()
                ),
                () -> assertNotNull(intervencion.getFin())
        );

        verify(intervencionRepository).save(intervencion);
        verify(solicitudRepository).save(solicitud);
    }

    private Solicitud crearSolicitud(
            EstadoSolicitud estado,
            String clienteId
    ) {
        Solicitud solicitud = new Solicitud();
        solicitud.setId(SOLICITUD_ID);
        solicitud.setClienteId(clienteId);
        solicitud.setEstado(estado);
        return solicitud;
    }

    private Asignacion crearAsignacion(
            Solicitud solicitud,
            Long tecnicoId
    ) {
        Asignacion asignacion = new Asignacion();
        asignacion.setSolicitud(solicitud);
        asignacion.setTecnicoId(tecnicoId);
        return asignacion;
    }
}
