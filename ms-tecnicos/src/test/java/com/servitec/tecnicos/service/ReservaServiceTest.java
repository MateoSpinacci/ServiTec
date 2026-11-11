package com.servitec.tecnicos.service;

import com.servitec.tecnicos.dto.reserva.request.ReservaRequest;
import com.servitec.tecnicos.dto.reserva.response.ReservaResponse;
import com.servitec.tecnicos.entity.Disponibilidad;
import com.servitec.tecnicos.entity.Reserva;
import com.servitec.tecnicos.entity.Tecnico;
import com.servitec.tecnicos.enums.EstadoReserva;
import com.servitec.tecnicos.repository.DisponibilidadRepository;
import com.servitec.tecnicos.repository.ReservaRepository;
import com.servitec.tecnicos.repository.TecnicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class ReservaServiceTest {
    private static final Long TECNICO_ID = 12L;
    private static final Long SOLICITUD_ID = 55L;
    private static final Long RESERVA_ID = 4L;
    private static final LocalDateTime INICIO = LocalDateTime.of(2030, 6, 10, 10, 0);
    private static final LocalDateTime FIN = INICIO.plusHours(1);

    @Mock ReservaRepository reservaRepository;
    @Mock TecnicoRepository tecnicoRepository;
    @Mock DisponibilidadRepository disponibilidadRepository;
    @InjectMocks ReservaService servicio;

    @Test
    void crearGuardaReservaActivaConDatosCorrectos() {
        Tecnico tecnico = prepararTecnicoDisponible();
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservaResponse respuesta = servicio.crear(request(INICIO, FIN));

        assertAll(() -> assertEquals(TECNICO_ID, respuesta.getTecnicoId()),
                () -> assertEquals(SOLICITUD_ID, respuesta.getSolicitudId()),
                () -> assertEquals(INICIO, respuesta.getInicio()),
                () -> assertEquals(FIN, respuesta.getFin()),
                () -> assertEquals(EstadoReserva.ACTIVA, respuesta.getEstado()));
        verify(reservaRepository).save(argThat(r -> r.getTecnico() == tecnico
                && SOLICITUD_ID.equals(r.getSolicitudId())
                && INICIO.equals(r.getInicio()) && FIN.equals(r.getFin())
                && r.getEstado() == EstadoReserva.ACTIVA));
    }

    @Test
    void crearRechazaIntervaloSinDuracion() {
        assertEstado(HttpStatus.BAD_REQUEST, () -> servicio.crear(request(INICIO, INICIO)));
        verifyNoInteractions(tecnicoRepository, disponibilidadRepository, reservaRepository);
    }

    @Test
    void crearRechazaFechaNula() {
        assertEstado(HttpStatus.BAD_REQUEST, () -> servicio.crear(request(null, FIN)));
        verifyNoInteractions(tecnicoRepository, disponibilidadRepository, reservaRepository);
    }

    @Test
    void crearRechazaTecnicoInexistente() {
        when(tecnicoRepository.findByIdForUpdate(TECNICO_ID)).thenReturn(Optional.empty());
        assertEstado(HttpStatus.NOT_FOUND, () -> servicio.crear(request(INICIO, FIN)));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void crearRechazaTecnicoInactivo() {
        when(tecnicoRepository.findByIdForUpdate(TECNICO_ID)).thenReturn(Optional.of(tecnico(false)));
        assertEstado(HttpStatus.CONFLICT, () -> servicio.crear(request(INICIO, FIN)));
        verifyNoInteractions(disponibilidadRepository, reservaRepository);
    }

    @Test
    void crearRechazaSiNoHayDisponibilidad() {
        when(tecnicoRepository.findByIdForUpdate(TECNICO_ID)).thenReturn(Optional.of(tecnico(true)));
        when(disponibilidadRepository.findByTecnicoId(TECNICO_ID)).thenReturn(List.of());
        assertEstado(HttpStatus.CONFLICT, () -> servicio.crear(request(INICIO, FIN)));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void crearRechazaReservaFueraDeDisponibilidad() {
        when(tecnicoRepository.findByIdForUpdate(TECNICO_ID)).thenReturn(Optional.of(tecnico(true)));
        when(disponibilidadRepository.findByTecnicoId(TECNICO_ID))
                .thenReturn(List.of(new Disponibilidad(tecnico(true), INICIO.plusMinutes(1), FIN.plusHours(1))));
        assertEstado(HttpStatus.CONFLICT, () -> servicio.crear(request(INICIO, FIN)));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void crearRechazaSuperposicionConReservaActiva() {
        prepararTecnicoDisponible();
        when(reservaRepository.existeSuperposicion(TECNICO_ID, INICIO, FIN, EstadoReserva.ACTIVA)).thenReturn(true);
        assertEstado(HttpStatus.CONFLICT, () -> servicio.crear(request(INICIO, FIN)));
        verify(reservaRepository, never()).save(any());
        verify(reservaRepository, never()).existsBySolicitudIdAndEstado(any(), any());
    }

    @Test
    void crearRechazaSolicitudConReservaActiva() {
        prepararTecnicoDisponible();
        when(reservaRepository.existsBySolicitudIdAndEstado(SOLICITUD_ID, EstadoReserva.ACTIVA)).thenReturn(true);
        assertEstado(HttpStatus.CONFLICT, () -> servicio.crear(request(INICIO, FIN)));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void crearAceptaReservaExactamenteEnLimitesDeDisponibilidad() {
        when(tecnicoRepository.findByIdForUpdate(TECNICO_ID)).thenReturn(Optional.of(tecnico(true)));
        when(disponibilidadRepository.findByTecnicoId(TECNICO_ID))
                .thenReturn(List.of(new Disponibilidad(tecnico(true), INICIO, FIN)));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));
        assertEquals(EstadoReserva.ACTIVA, servicio.crear(request(INICIO, FIN)).getEstado());
    }

    @Test
    void liberarCambiaEstadoDeReservaActiva() {
        Reserva reserva = reserva(EstadoReserva.ACTIVA);
        when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.of(reserva));
        servicio.liberarReserva(RESERVA_ID);
        assertEquals(EstadoReserva.LIBERADA, reserva.getEstado());
        verify(reservaRepository).save(reserva);
    }

    @Test
    void liberarReservaYaLiberadaEsIdempotente() {
        Reserva reserva = reserva(EstadoReserva.LIBERADA);
        when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.of(reserva));
        servicio.liberarReserva(RESERVA_ID);
        assertEquals(EstadoReserva.LIBERADA, reserva.getEstado());
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void liberarReservaInexistenteDevuelve404() {
        when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.empty());
        assertEstado(HttpStatus.NOT_FOUND, () -> servicio.liberarReserva(RESERVA_ID));
        verify(reservaRepository, never()).save(any());
    }

    private Tecnico prepararTecnicoDisponible() {
        Tecnico tecnico = tecnico(true);
        when(tecnicoRepository.findByIdForUpdate(TECNICO_ID)).thenReturn(Optional.of(tecnico));
        when(disponibilidadRepository.findByTecnicoId(TECNICO_ID))
                .thenReturn(List.of(new Disponibilidad(tecnico, INICIO.minusHours(1), FIN.plusHours(1))));
        return tecnico;
    }

    private Tecnico tecnico(boolean activo) {
        Tecnico tecnico = new Tecnico();
        tecnico.setId(TECNICO_ID);
        tecnico.setActivo(activo);
        return tecnico;
    }

    private Reserva reserva(EstadoReserva estado) {
        Reserva reserva = new Reserva();
        reserva.setEstado(estado);
        return reserva;
    }

    private ReservaRequest request(LocalDateTime inicio, LocalDateTime fin) {
        return new ReservaRequest(TECNICO_ID, SOLICITUD_ID, inicio, fin);
    }

    private void assertEstado(HttpStatus estado, org.junit.jupiter.api.function.Executable operacion) {
        assertEquals(estado, assertThrows(ResponseStatusException.class, operacion).getStatusCode());
    }
}
