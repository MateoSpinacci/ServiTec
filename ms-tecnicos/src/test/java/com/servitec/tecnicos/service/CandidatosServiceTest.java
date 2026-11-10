package com.servitec.tecnicos.service;

// Clases de la aplicación: cliente de rutas, datos de búsqueda/respuesta, entidades y repositorios.
import com.servitec.tecnicos.client.RutasClient;
import com.servitec.tecnicos.dto.tecnico.request.CandidatosRequest;
import com.servitec.tecnicos.dto.tecnico.response.CandidatoResponse;
import com.servitec.tecnicos.entity.Disponibilidad;
import com.servitec.tecnicos.entity.Tecnico;
import com.servitec.tecnicos.enums.EstadoReserva;
import com.servitec.tecnicos.repository.DisponibilidadRepository;
import com.servitec.tecnicos.repository.ReservaRepository;
import com.servitec.tecnicos.repository.TecnicoRepository;
// JUnit define y ejecuta los casos de prueba.
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
// Mockito simula repositorios y cliente de rutas, y los inyecta en el servicio.
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// Fecha y lista de Java usadas para preparar horarios y resultados.
import java.time.LocalDateTime;
import java.util.List;

// Aserciones comprueban candidatos y Mockito configura las dependencias simuladas.
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    // Comprueba que se incluya un técnico disponible con un traslado de hasta 45 minutos.
    @Test
    void buscarCandidatosDevuelveTecnicoDisponibleDentroDelTiempoDeTraslado() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        CandidatosRequest request = new CandidatosRequest();
        request.setEspecialidadId(3L);
        request.setFechaHoraSolicitada(inicio);
        request.setDuracionEstimadaMin(60);
        request.setLatitud(-34.7);
        request.setLongitud(-58.5);

        Tecnico tecnico = new Tecnico();
        tecnico.setId(12L);
        tecnico.setNombre("Ana");
        tecnico.setLatitudBase(-34.6);
        tecnico.setLongitudBase(-58.4);
        Disponibilidad disponibilidad = new Disponibilidad(
                tecnico,
                inicio.minusHours(1),
                inicio.plusHours(2)
        );
        when(tecnicoRepository.buscarActivosPorEspecialidad(3L))
                .thenReturn(List.of(tecnico));
        when(disponibilidadRepository.findByTecnicoId(12L))
                .thenReturn(List.of(disponibilidad));
        when(reservaRepository.existeSuperposicion(
                12L,
                inicio,
                inicio.plusMinutes(60),
                EstadoReserva.ACTIVA
        )).thenReturn(false);
        when(rutasClient.calcularDuracionSegundos(-34.6, -58.4, -34.7, -58.5))
                .thenReturn(61.0);

        List<CandidatoResponse> candidatos = candidatosService.buscarCandidatos(request);

        assertEquals(1, candidatos.size());
        assertEquals(12L, candidatos.get(0).getTecnicoId());
        assertEquals("Ana", candidatos.get(0).getNombre());
        assertEquals(2L, candidatos.get(0).getTiempoTrasladoMinutos());
    }

    // Comprueba que se excluya a un técnico cuyo traslado supera el límite de 45 minutos.
    @Test
    void buscarCandidatosExcluyeTecnicoConTrasladoMayorA45Minutos() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        CandidatosRequest request = new CandidatosRequest();
        request.setEspecialidadId(3L);
        request.setFechaHoraSolicitada(inicio);
        request.setDuracionEstimadaMin(60);
        request.setLatitud(-34.7);
        request.setLongitud(-58.5);

        Tecnico tecnico = new Tecnico();
        tecnico.setId(12L);
        tecnico.setLatitudBase(-34.6);
        tecnico.setLongitudBase(-58.4);
        Disponibilidad disponibilidad = new Disponibilidad(
                tecnico,
                inicio.minusHours(1),
                inicio.plusHours(2)
        );
        when(tecnicoRepository.buscarActivosPorEspecialidad(3L))
                .thenReturn(List.of(tecnico));
        when(disponibilidadRepository.findByTecnicoId(12L))
                .thenReturn(List.of(disponibilidad));
        when(reservaRepository.existeSuperposicion(
                12L,
                inicio,
                inicio.plusMinutes(60),
                EstadoReserva.ACTIVA
        )).thenReturn(false);
        when(rutasClient.calcularDuracionSegundos(-34.6, -58.4, -34.7, -58.5))
                .thenReturn(45 * 60 + 1.0);

        List<CandidatoResponse> candidatos = candidatosService.buscarCandidatos(request);

        assertEquals(0, candidatos.size());
    }
}
