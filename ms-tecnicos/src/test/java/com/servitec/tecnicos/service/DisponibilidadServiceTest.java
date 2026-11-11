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
class DisponibilidadServiceTest {
    private static final Long TECNICO_ID = 12L;
    private static final Long DISPONIBILIDAD_ID = 4L;
    private static final LocalDateTime INICIO = LocalDateTime.of(2030, 6, 10, 9, 0);
    private static final LocalDateTime FIN = INICIO.plusHours(4);

    @Mock DisponibilidadRepository disponibilidadRepository;
    @Mock TecnicoRepository tecnicoRepository;
    @Mock ReservaRepository reservaRepository;
    @InjectMocks DisponibilidadService servicio;

    @Test
    void crearGuardaDisponibilidadValida() {
        Tecnico tecnico = tecnico(true);
        when(tecnicoRepository.findByIdForUpdate(TECNICO_ID)).thenReturn(Optional.of(tecnico));
        when(disponibilidadRepository.findByTecnicoId(TECNICO_ID)).thenReturn(List.of());
        when(disponibilidadRepository.save(any(Disponibilidad.class))).thenAnswer(inv -> inv.getArgument(0));

        DisponibilidadResponse respuesta = servicio.crear(TECNICO_ID, request(INICIO, FIN));

        assertAll(() -> assertEquals(TECNICO_ID, respuesta.getTecnicoId()),
                () -> assertEquals(INICIO, respuesta.getInicio()),
                () -> assertEquals(FIN, respuesta.getFin()));
        verify(disponibilidadRepository).save(argThat(d -> d.getTecnico() == tecnico
                && INICIO.equals(d.getInicio()) && FIN.equals(d.getFin())));
    }

    @Test
    void crearRechazaFechasIguales() {
        assertEstado(HttpStatus.BAD_REQUEST, () -> servicio.crear(TECNICO_ID, request(INICIO, INICIO)));
        verifyNoInteractions(tecnicoRepository, disponibilidadRepository, reservaRepository);
    }

    @Test
    void crearRechazaFechasNulas() {
        assertEstado(HttpStatus.BAD_REQUEST, () -> servicio.crear(TECNICO_ID, request(null, FIN)));
        verifyNoInteractions(tecnicoRepository, disponibilidadRepository, reservaRepository);
    }

    @Test
    void crearRechazaTecnicoInexistente() {
        when(tecnicoRepository.findByIdForUpdate(TECNICO_ID)).thenReturn(Optional.empty());
        assertEstado(HttpStatus.NOT_FOUND, () -> servicio.crear(TECNICO_ID, request(INICIO, FIN)));
        verify(disponibilidadRepository, never()).save(any());
    }

    @Test
    void crearRechazaTecnicoInactivo() {
        when(tecnicoRepository.findByIdForUpdate(TECNICO_ID)).thenReturn(Optional.of(tecnico(false)));
        assertEstado(HttpStatus.CONFLICT, () -> servicio.crear(TECNICO_ID, request(INICIO, FIN)));
        verifyNoInteractions(disponibilidadRepository, reservaRepository);
    }

    @Test
    void crearRechazaSuperposicion() {
        prepararTecnicoActivo();
        when(disponibilidadRepository.findByTecnicoId(TECNICO_ID)).thenReturn(List.of(disponibilidad(INICIO, FIN)));
        assertEstado(HttpStatus.CONFLICT, () -> servicio.crear(TECNICO_ID, request(INICIO.plusHours(2), FIN.plusHours(2))));
        verify(disponibilidadRepository, never()).save(any());
    }

    @Test
    void crearAceptaIntervaloAdyacente() {
        prepararTecnicoActivo();
        when(disponibilidadRepository.findByTecnicoId(TECNICO_ID)).thenReturn(List.of(disponibilidad(INICIO, FIN)));
        when(disponibilidadRepository.save(any(Disponibilidad.class))).thenAnswer(inv -> inv.getArgument(0));
        assertEquals(FIN, servicio.crear(TECNICO_ID, request(FIN, FIN.plusHours(2))).getInicio());
    }

    @Test
    void listarRechazaTecnicoInexistente() {
        when(tecnicoRepository.existsById(TECNICO_ID)).thenReturn(false);
        assertEstado(HttpStatus.NOT_FOUND, () -> servicio.listar(TECNICO_ID));
        verifyNoInteractions(disponibilidadRepository);
    }

    @Test
    void listarDevuelveDisponibilidadesDelTecnico() {
        when(tecnicoRepository.existsById(TECNICO_ID)).thenReturn(true);
        when(disponibilidadRepository.findByTecnicoId(TECNICO_ID)).thenReturn(List.of(disponibilidad(INICIO, FIN)));
        assertEquals(1, servicio.listar(TECNICO_ID).size());
    }

    @Test
    void actualizarPermiteModificarSinReservas() {
        prepararTecnicoActivo();
        Disponibilidad existente = disponibilidad(INICIO, FIN);
        when(disponibilidadRepository.findById(DISPONIBILIDAD_ID)).thenReturn(Optional.of(existente));
        when(disponibilidadRepository.findByTecnicoId(TECNICO_ID)).thenReturn(List.of(existente));
        when(reservaRepository.findByTecnicoIdAndEstado(TECNICO_ID, EstadoReserva.ACTIVA)).thenReturn(List.of());
        when(disponibilidadRepository.save(existente)).thenReturn(existente);

        DisponibilidadResponse respuesta = servicio.actualizarDisponibilidad(TECNICO_ID, DISPONIBILIDAD_ID,
                request(INICIO.plusHours(1), FIN.plusHours(1)));

        assertEquals(INICIO.plusHours(1), respuesta.getInicio());
        assertEquals(FIN.plusHours(1), existente.getFin());
        verify(disponibilidadRepository).save(existente);
    }

    @Test
    void actualizarRechazaDisponibilidadAjena() {
        prepararTecnicoActivo();
        Disponibilidad ajena = disponibilidad(INICIO, FIN);
        ajena.getTecnico().setId(99L);
        when(disponibilidadRepository.findById(DISPONIBILIDAD_ID)).thenReturn(Optional.of(ajena));
        assertEstado(HttpStatus.NOT_FOUND, () -> servicio.actualizarDisponibilidad(TECNICO_ID, DISPONIBILIDAD_ID, request(INICIO, FIN)));
        verify(disponibilidadRepository, never()).save(any());
    }

    @Test
    void actualizarRechazaSuperposicionConOtraDisponibilidad() {
        prepararTecnicoActivo();
        Disponibilidad existente = disponibilidad(INICIO, FIN);
        Disponibilidad otra = disponibilidad(FIN.plusHours(1), FIN.plusHours(3));
        otra.setId(8L);
        when(disponibilidadRepository.findById(DISPONIBILIDAD_ID)).thenReturn(Optional.of(existente));
        when(disponibilidadRepository.findByTecnicoId(TECNICO_ID)).thenReturn(List.of(existente, otra));
        assertEstado(HttpStatus.CONFLICT, () -> servicio.actualizarDisponibilidad(TECNICO_ID, DISPONIBILIDAD_ID,
                request(INICIO.plusHours(2), FIN.plusHours(2))));
        verify(disponibilidadRepository, never()).save(any());
    }

    @Test
    void actualizarRechazaSiDejaReservaActivaSinCobertura() {
        prepararTecnicoActivo();
        Disponibilidad existente = disponibilidad(INICIO, FIN);
        when(disponibilidadRepository.findById(DISPONIBILIDAD_ID)).thenReturn(Optional.of(existente));
        when(disponibilidadRepository.findByTecnicoId(TECNICO_ID)).thenReturn(List.of(existente));
        when(reservaRepository.findByTecnicoIdAndEstado(TECNICO_ID, EstadoReserva.ACTIVA))
                .thenReturn(List.of(reserva(INICIO.plusHours(1), FIN.minusHours(1))));

        assertEstado(HttpStatus.CONFLICT, () -> servicio.actualizarDisponibilidad(TECNICO_ID, DISPONIBILIDAD_ID,
                request(INICIO.plusHours(2), FIN)));
        assertEquals(INICIO, existente.getInicio());
        verify(disponibilidadRepository, never()).save(any());
    }

    @Test
    void actualizarPermiteCambioSiReservaActivaSigueCubierta() {
        prepararTecnicoActivo();
        Disponibilidad existente = disponibilidad(INICIO, FIN);
        when(disponibilidadRepository.findById(DISPONIBILIDAD_ID)).thenReturn(Optional.of(existente));
        when(disponibilidadRepository.findByTecnicoId(TECNICO_ID)).thenReturn(List.of(existente));
        when(reservaRepository.findByTecnicoIdAndEstado(TECNICO_ID, EstadoReserva.ACTIVA))
                .thenReturn(List.of(reserva(INICIO.plusHours(1), FIN.minusHours(1))));
        when(disponibilidadRepository.save(existente)).thenReturn(existente);

        servicio.actualizarDisponibilidad(TECNICO_ID, DISPONIBILIDAD_ID, request(INICIO.plusMinutes(30), FIN.minusMinutes(30)));
        verify(disponibilidadRepository).save(existente);
    }

    private void prepararTecnicoActivo() {
        when(tecnicoRepository.findByIdForUpdate(TECNICO_ID)).thenReturn(Optional.of(tecnico(true)));
    }

    private Tecnico tecnico(boolean activo) {
        Tecnico tecnico = new Tecnico();
        tecnico.setId(TECNICO_ID);
        tecnico.setActivo(activo);
        return tecnico;
    }

    private Disponibilidad disponibilidad(LocalDateTime inicio, LocalDateTime fin) {
        Disponibilidad disponibilidad = new Disponibilidad(tecnico(true), inicio, fin);
        disponibilidad.setId(DISPONIBILIDAD_ID);
        return disponibilidad;
    }

    private Reserva reserva(LocalDateTime inicio, LocalDateTime fin) {
        Reserva reserva = new Reserva();
        reserva.setInicio(inicio);
        reserva.setFin(fin);
        reserva.setEstado(EstadoReserva.ACTIVA);
        return reserva;
    }

    private DisponibilidadRequest request(LocalDateTime inicio, LocalDateTime fin) {
        return new DisponibilidadRequest(inicio, fin);
    }

    private void assertEstado(HttpStatus estado, org.junit.jupiter.api.function.Executable operacion) {
        assertEquals(estado, assertThrows(ResponseStatusException.class, operacion).getStatusCode());
    }
}
