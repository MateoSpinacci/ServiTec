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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidatosServiceTest {

    @Mock
    private TecnicoRepository tecnicoRepository;

    @Mock
    private DisponibilidadRepository disponibilidadRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private RutasClient rutasClient;

    @InjectMocks
    private CandidatosService candidatosService;

    private static final Long TECNICO_ID = 12L;
    private static final Long ESPECIALIDAD_ID = 3L;

    private static final double LAT_BASE = -34.6;
    private static final double LON_BASE = -58.4;
    private static final double LAT_DESTINO = -34.7;
    private static final double LON_DESTINO = -58.5;

    private CandidatosRequest crearRequest(LocalDateTime inicio) {
        CandidatosRequest request = new CandidatosRequest();
        request.setEspecialidadId(ESPECIALIDAD_ID);
        request.setFechaHoraSolicitada(inicio);
        request.setDuracionEstimadaMin(60);
        request.setLatitud(LAT_DESTINO);
        request.setLongitud(LON_DESTINO);
        return request;
    }

    private Tecnico crearTecnico() {
        Tecnico tecnico = new Tecnico();
        tecnico.setId(TECNICO_ID);
        tecnico.setNombre("Ana");
        tecnico.setActivo(true);
        tecnico.setLatitudBase(LAT_BASE);
        tecnico.setLongitudBase(LON_BASE);
        return tecnico;
    }

    private Disponibilidad crearDisponibilidad(
            Tecnico tecnico,
            LocalDateTime inicio,
            LocalDateTime fin
    ) {
        return new Disponibilidad(tecnico, inicio, fin);
    }

    private void configurarTecnicoDisponible(
            Tecnico tecnico,
            LocalDateTime inicio
    ) {
        when(tecnicoRepository.buscarActivosPorEspecialidad(ESPECIALIDAD_ID))
                .thenReturn(List.of(tecnico));

        when(disponibilidadRepository.findByTecnicoId(TECNICO_ID))
                .thenReturn(List.of(
                        crearDisponibilidad(
                                tecnico,
                                inicio.minusHours(1),
                                inicio.plusHours(2)
                        )
                ));

        when(reservaRepository.existeSuperposicion(
                TECNICO_ID,
                inicio,
                inicio.plusMinutes(60),
                EstadoReserva.ACTIVA
        )).thenReturn(false);
    }

    private void configurarTiempoTraslado(double segundos) {
        when(rutasClient.calcularDuracionSegundos(
                LAT_BASE, LON_BASE, LAT_DESTINO, LON_DESTINO
        )).thenReturn(segundos);
    }

    @Test
    void buscarCandidatosIncluyeTecnicoDisponible() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        Tecnico tecnico = crearTecnico();

        configurarTecnicoDisponible(tecnico, inicio);
        configurarTiempoTraslado(61.0);

        List<CandidatoResponse> candidatos =
                candidatosService.buscarCandidatos(crearRequest(inicio));

        assertEquals(1, candidatos.size());
        assertEquals(TECNICO_ID, candidatos.get(0).getTecnicoId());
        assertEquals("Ana", candidatos.get(0).getNombre());
        assertEquals(2L, candidatos.get(0).getTiempoTrasladoMinutos());
    }

    @Test
    void buscarCandidatosIncluyeTecnicoConTrasladoExactamente45Minutos() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        Tecnico tecnico = crearTecnico();

        configurarTecnicoDisponible(tecnico, inicio);
        configurarTiempoTraslado(2700.0);

        List<CandidatoResponse> candidatos =
                candidatosService.buscarCandidatos(crearRequest(inicio));

        assertEquals(1, candidatos.size());
        assertEquals(45L, candidatos.get(0).getTiempoTrasladoMinutos());
    }

    @Test
    void buscarCandidatosExcluyeTecnicoConTrasladoMayorA45Minutos() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        Tecnico tecnico = crearTecnico();

        configurarTecnicoDisponible(tecnico, inicio);
        configurarTiempoTraslado(2701.0);

        List<CandidatoResponse> candidatos =
                candidatosService.buscarCandidatos(crearRequest(inicio));

        assertTrue(candidatos.isEmpty());
    }

    @Test
    void buscarCandidatosExcluyeTecnicoSinDisponibilidad() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        Tecnico tecnico = crearTecnico();

        when(tecnicoRepository.buscarActivosPorEspecialidad(ESPECIALIDAD_ID))
                .thenReturn(List.of(tecnico));

        when(disponibilidadRepository.findByTecnicoId(TECNICO_ID))
                .thenReturn(List.of());

        List<CandidatoResponse> candidatos =
                candidatosService.buscarCandidatos(crearRequest(inicio));

        assertTrue(candidatos.isEmpty());
        verifyNoInteractions(rutasClient);
    }

    @Test
    void buscarCandidatosExcluyeTecnicoConDisponibilidadInsuficiente() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        Tecnico tecnico = crearTecnico();

        when(tecnicoRepository.buscarActivosPorEspecialidad(ESPECIALIDAD_ID))
                .thenReturn(List.of(tecnico));

        when(disponibilidadRepository.findByTecnicoId(TECNICO_ID))
                .thenReturn(List.of(
                        crearDisponibilidad(
                                tecnico,
                                inicio.minusMinutes(30),
                                inicio.plusMinutes(30)
                        )
                ));

        List<CandidatoResponse> candidatos =
                candidatosService.buscarCandidatos(crearRequest(inicio));

        assertTrue(candidatos.isEmpty());
        verifyNoInteractions(rutasClient);
    }

    @Test
    void buscarCandidatosExcluyeTecnicoConReservaSuperpuesta() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        Tecnico tecnico = crearTecnico();

        when(tecnicoRepository.buscarActivosPorEspecialidad(ESPECIALIDAD_ID))
                .thenReturn(List.of(tecnico));

        when(disponibilidadRepository.findByTecnicoId(TECNICO_ID))
                .thenReturn(List.of(
                        crearDisponibilidad(
                                tecnico,
                                inicio.minusHours(1),
                                inicio.plusHours(2)
                        )
                ));

        when(reservaRepository.existeSuperposicion(
                TECNICO_ID,
                inicio,
                inicio.plusMinutes(60),
                EstadoReserva.ACTIVA
        )).thenReturn(true);

        List<CandidatoResponse> candidatos =
                candidatosService.buscarCandidatos(crearRequest(inicio));

        assertTrue(candidatos.isEmpty());
        verifyNoInteractions(rutasClient);
    }

    @Test
    void buscarCandidatosDevuelveListaVaciaSiNoHayTecnicosDeLaEspecialidad() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);

        when(tecnicoRepository.buscarActivosPorEspecialidad(ESPECIALIDAD_ID))
                .thenReturn(List.of());

        List<CandidatoResponse> candidatos =
                candidatosService.buscarCandidatos(crearRequest(inicio));

        assertNotNull(candidatos);
        assertTrue(candidatos.isEmpty());

        verifyNoInteractions(
                disponibilidadRepository,
                reservaRepository,
                rutasClient
        );
    }
}