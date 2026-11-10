package com.servitec.solicitudes.service;

// Clases de la aplicación que usa el servicio bajo prueba: cliente, DTOs, entidades, estados y repositorios.
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
// JUnit define y ejecuta los casos de prueba.
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
// Mockito crea dependencias simuladas y las conecta al servicio para no usar base de datos ni otros servicios.
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
// Tipos de Spring para verificar el estado HTTP de errores del servicio.
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

// Utilidades estándar de Java usadas en fechas y colecciones de prueba.
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Aserciones comprueban resultados; las funciones Mockito configuran y verifican las dependencias simuladas.
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudServiceTest {

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

    // Comprueba que se copien los datos de entrada y que la nueva solicitud quede pendiente.
    @Test
    void crearGuardaSolicitudPendienteConDatosDelRequest() {
        LocalDateTime fechaDeseada = LocalDateTime.now().plusDays(1);
        SolicitudRequest request = new SolicitudRequest();
        request.setEspecialidadId(3L);
        request.setDescripcion("Reparar una canilla");
        request.setDireccion("Calle 123");
        request.setLatitud(-34.6);
        request.setLongitud(-58.4);
        request.setFechaDeseada(fechaDeseada);
        request.setDuracionEstimada(60);
        when(solicitudRepository.save(any(Solicitud.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Solicitud creada = solicitudService.crear(request, 8L);

        assertEquals(8L, creada.getClienteId());
        assertEquals(3L, creada.getEspecialidadId());
        assertEquals("Reparar una canilla", creada.getDescripcion());
        assertEquals("Calle 123", creada.getDireccion());
        assertEquals(-34.6, creada.getLatitud());
        assertEquals(-58.4, creada.getLongitud());
        assertEquals(fechaDeseada, creada.getFechaSolicitud());
        assertEquals(60, creada.getDuracionEstimada());
        assertEquals(EstadoSolicitud.PENDIENTE, creada.getEstado());
        assertNotNull(creada.getFechaCreacion());
        verify(solicitudRepository).save(creada);
    }

    // Comprueba que el cliente dueño pueda cancelar una solicitud pendiente.
    @Test
    void cancelarCambiaEstadoDeSolicitudPendienteDelCliente() {
        Solicitud solicitud = solicitud(10L, 8L, EstadoSolicitud.PENDIENTE);
        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(solicitud)).thenReturn(solicitud);

        Solicitud cancelada = solicitudService.cancelar(10L, 8L);

        assertEquals(EstadoSolicitud.CANCELADA, cancelada.getEstado());
        verify(solicitudRepository).save(solicitud);
    }

    // Comprueba que otro cliente no pueda cancelar la solicitud.
    @Test
    void cancelarFallaSiLaSolicitudNoPerteneceAlCliente() {
        when(solicitudRepository.findById(10L))
                .thenReturn(Optional.of(solicitud(10L, 99L, EstadoSolicitud.PENDIENTE)));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> solicitudService.cancelar(10L, 8L)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(solicitudRepository, never()).save(any(Solicitud.class));
    }

    // Comprueba que se cree la asignación y que la solicitud pase al estado asignado.
    @Test
    void asignarTecnicoGuardaAsignacionYActualizaEstado() {
        Solicitud solicitud = solicitud(10L, 8L, EstadoSolicitud.PENDIENTE);
        solicitud.setEspecialidadId(3L);
        solicitud.setFechaSolicitud(LocalDateTime.now().plusDays(1));
        solicitud.setDuracionEstimada(60);
        AsignacionRequest request = new AsignacionRequest();
        request.setTecnicoId(20L);

        TecnicoResponse tecnico = mock(TecnicoResponse.class);
        EspecialidadResponse especialidad = mock(EspecialidadResponse.class);
        ReservaResponse reserva = mock(ReservaResponse.class);
        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        when(asignacionRepository.existsBySolicitudId(10L)).thenReturn(false);
        when(tecnicosClient.obtenerPorId(20L)).thenReturn(tecnico);
        when(tecnico.isActivo()).thenReturn(true);
        when(tecnico.getEspecialidades()).thenReturn(List.of(especialidad));
        when(especialidad.getId()).thenReturn(3L);
        when(tecnicosClient.crearReserva(any(ReservaRequest.class))).thenReturn(reserva);
        when(reserva.getId()).thenReturn(30L);
        when(asignacionRepository.saveAndFlush(any(Asignacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(solicitudRepository.saveAndFlush(solicitud)).thenReturn(solicitud);

        Solicitud asignada = solicitudService.asignarTecnico(10L, request);

        assertEquals(EstadoSolicitud.ASIGNADA, asignada.getEstado());
        ArgumentCaptor<Asignacion> asignacionCaptor = ArgumentCaptor.forClass(Asignacion.class);
        verify(asignacionRepository).saveAndFlush(asignacionCaptor.capture());
        assertEquals(solicitud, asignacionCaptor.getValue().getSolicitud());
        assertEquals(20L, asignacionCaptor.getValue().getTecnicoId());
        assertEquals(30L, asignacionCaptor.getValue().getReservaId());
        verify(tecnicosClient).crearReserva(any(ReservaRequest.class));
        verify(solicitudRepository).saveAndFlush(solicitud);
    }

    // Comprueba que iniciar un trabajo cree una intervención y cambie su estado a en curso.
    @Test
    void iniciarTrabajoGuardaIntervencionYCambiaEstado() {
        Solicitud solicitud = solicitud(10L, 8L, EstadoSolicitud.ASIGNADA);
        Asignacion asignacion = asignacion(solicitud, 20L);
        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        when(asignacionRepository.findBySolicitudId(10L))
                .thenReturn(Optional.of(asignacion));
        when(solicitudRepository.save(solicitud)).thenReturn(solicitud);

        Solicitud enCurso = solicitudService.iniciarTrabajo(10L, 20L);

        assertEquals(EstadoSolicitud.EN_CURSO, enCurso.getEstado());
        ArgumentCaptor<Intervencion> intervencionCaptor =
                ArgumentCaptor.forClass(Intervencion.class);
        verify(intervencionRepository).save(intervencionCaptor.capture());
        assertEquals(solicitud, intervencionCaptor.getValue().getSolicitud());
        assertEquals(asignacion, intervencionCaptor.getValue().getAsignacion());
        assertNotNull(intervencionCaptor.getValue().getInicio());
        verify(solicitudRepository).save(solicitud);
    }

    // Comprueba que resolver un trabajo guarde el diagnóstico, la resolución y el estado final.
    @Test
    void resolverTrabajoGuardaDiagnosticoResolucionYEstadoFinal() {
        Solicitud solicitud = solicitud(10L, 8L, EstadoSolicitud.EN_CURSO);
        Asignacion asignacion = asignacion(solicitud, 20L);
        Intervencion intervencion = new Intervencion();
        ResolucionRequest request = new ResolucionRequest();
        request.setDiagnostico("Pérdida en la conexión");
        request.setResolucion("Se reemplazó el flexible");
        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        when(asignacionRepository.findBySolicitudId(10L))
                .thenReturn(Optional.of(asignacion));
        when(intervencionRepository.findBySolicitudId(10L))
                .thenReturn(Optional.of(intervencion));
        when(solicitudRepository.save(solicitud)).thenReturn(solicitud);

        Solicitud resuelta = solicitudService.resolverTrabajo(10L, 20L, request);

        assertEquals(EstadoSolicitud.RESUELTA, resuelta.getEstado());
        assertEquals("Pérdida en la conexión", intervencion.getDiagnostico());
        assertEquals("Se reemplazó el flexible", intervencion.getResolucion());
        assertNotNull(intervencion.getFin());
        verify(intervencionRepository).save(intervencion);
        verify(solicitudRepository).save(solicitud);
    }

    // Comprueba que se informe un error 404 si se intenta operar sobre una solicitud inexistente.
    @Test
    void buscarSolicitudInexistenteDevuelveNotFound() {
        when(solicitudRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> solicitudService.cancelar(10L, 8L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(solicitudRepository, never()).save(any(Solicitud.class));
    }

    private Solicitud solicitud(Long id, Long clienteId, EstadoSolicitud estado) {
        Solicitud solicitud = new Solicitud();
        solicitud.setId(id);
        solicitud.setClienteId(clienteId);
        solicitud.setEstado(estado);
        return solicitud;
    }

    private Asignacion asignacion(Solicitud solicitud, Long tecnicoId) {
        Asignacion asignacion = new Asignacion();
        asignacion.setSolicitud(solicitud);
        asignacion.setTecnicoId(tecnicoId);
        return asignacion;
    }
}
