package com.servitec.tecnicos.service;

// Clases de la aplicación: DTOs, entidades, estado y repositorios usados por el servicio.
import com.servitec.tecnicos.dto.reserva.request.ReservaRequest;
import com.servitec.tecnicos.dto.reserva.response.ReservaResponse;
import com.servitec.tecnicos.entity.Disponibilidad;
import com.servitec.tecnicos.entity.Reserva;
import com.servitec.tecnicos.entity.Tecnico;
import com.servitec.tecnicos.enums.EstadoReserva;
import com.servitec.tecnicos.repository.DisponibilidadRepository;
import com.servitec.tecnicos.repository.ReservaRepository;
import com.servitec.tecnicos.repository.TecnicoRepository;
// JUnit define y ejecuta los casos de prueba.
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
// Mockito simula los repositorios y los inyecta en el servicio.
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
// Tipos de Spring para comprobar el estado HTTP de las excepciones.
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

// Fechas, listas y Optional de Java usados para preparar escenarios.
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Aserciones validan resultados y Mockito configura/verifica los repositorios simulados.
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private TecnicoRepository tecnicoRepository;

    @Mock
    private DisponibilidadRepository disponibilidadRepository;

    @InjectMocks
    private ReservaService reservaService;

    // Comprueba que se cree una reserva activa cuando el horario está disponible y no se superpone.
    @Test
    void crearGuardaReservaSiTecnicoEstaDisponible() {
        Long tecnicoId = 12L;
        Long solicitudId = 55L;
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        LocalDateTime fin = inicio.plusHours(1);
        ReservaRequest request = new ReservaRequest(tecnicoId, solicitudId, inicio, fin);
        Tecnico tecnico = new Tecnico();
        tecnico.setId(tecnicoId);
        tecnico.setActivo(true);
        Disponibilidad disponibilidad = new Disponibilidad(
                tecnico,
                inicio.minusHours(1),
                fin.plusHours(1)
        );
        when(tecnicoRepository.findByIdForUpdate(tecnicoId)).thenReturn(Optional.of(tecnico));
        when(disponibilidadRepository.findByTecnicoId(tecnicoId))
                .thenReturn(List.of(disponibilidad));
        when(reservaRepository.existeSuperposicion(
                tecnicoId, inicio, fin, EstadoReserva.ACTIVA
        )).thenReturn(false);
        when(reservaRepository.existsBySolicitudIdAndEstado(
                solicitudId, EstadoReserva.ACTIVA
        )).thenReturn(false);
        when(reservaRepository.save(any(Reserva.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReservaResponse response = reservaService.crear(request);

        assertEquals(tecnicoId, response.getTecnicoId());
        assertEquals(solicitudId, response.getSolicitudId());
        assertEquals(inicio, response.getInicio());
        assertEquals(fin, response.getFin());
        assertEquals(EstadoReserva.ACTIVA, response.getEstado());
        verify(reservaRepository).save(any(Reserva.class));
    }

    // Comprueba que se rechace un intervalo de reserva inválido sin consultar dependencias.
    @Test
    void crearFallaSiElIntervaloEsInvalido() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        ReservaRequest request = new ReservaRequest(12L, 55L, inicio, inicio);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reservaService.crear(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(tecnicoRepository, disponibilidadRepository, reservaRepository);
    }

    // Comprueba que liberar una reserva activa cambie su estado y guarde la modificación.
    @Test
    void liberarCambiaEstadoDeReservaActiva() {
        Reserva reserva = new Reserva();
        reserva.setEstado(EstadoReserva.ACTIVA);
        when(reservaRepository.findById(4L)).thenReturn(Optional.of(reserva));

        reservaService.liberarReserva(4L);

        assertEquals(EstadoReserva.LIBERADA, reserva.getEstado());
        verify(reservaRepository).save(reserva);
    }
}
