package com.servitec.tecnicos.service;

// Clases de la aplicación: DTOs, entidades y repositorios usados al crear disponibilidad.
import com.servitec.tecnicos.dto.disponibilidad.request.DisponibilidadRequest;
import com.servitec.tecnicos.dto.disponibilidad.response.DisponibilidadResponse;
import com.servitec.tecnicos.entity.Disponibilidad;
import com.servitec.tecnicos.entity.Tecnico;
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
class DisponibilidadServiceTest {

    @Mock
    private DisponibilidadRepository disponibilidadRepository;

    @Mock
    private TecnicoRepository tecnicoRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private DisponibilidadService disponibilidadService;

    // Comprueba que se guarde el horario solicitado para un técnico activo.
    @Test
    void crearGuardaDisponibilidadDeTecnicoActivo() {
        Long tecnicoId = 12L;
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        LocalDateTime fin = inicio.plusHours(4);
        DisponibilidadRequest request = new DisponibilidadRequest(inicio, fin);
        Tecnico tecnico = new Tecnico();
        tecnico.setId(tecnicoId);
        tecnico.setActivo(true);
        when(tecnicoRepository.findByIdForUpdate(tecnicoId)).thenReturn(Optional.of(tecnico));
        when(disponibilidadRepository.findByTecnicoId(tecnicoId)).thenReturn(List.of());
        when(disponibilidadRepository.save(any(Disponibilidad.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DisponibilidadResponse response = disponibilidadService.crear(tecnicoId, request);

        assertEquals(tecnicoId, response.getTecnicoId());
        assertEquals(inicio, response.getInicio());
        assertEquals(fin, response.getFin());
        verify(disponibilidadRepository).save(any(Disponibilidad.class));
    }

    // Comprueba que un intervalo cuya fecha final no supera a la inicial se rechace con 400.
    @Test
    void crearFallaSiElIntervaloNoEsValido() {
        Long tecnicoId = 12L;
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        DisponibilidadRequest request = new DisponibilidadRequest(inicio, inicio);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> disponibilidadService.crear(tecnicoId, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(tecnicoRepository, disponibilidadRepository);
    }

    // Comprueba que no se guarde un horario que se cruza con otra disponibilidad existente.
    @Test
    void crearFallaSiSeSuperponeConOtraDisponibilidad() {
        Long tecnicoId = 12L;
        LocalDateTime inicioExistente = LocalDateTime.now().plusDays(1);
        Tecnico tecnico = new Tecnico();
        tecnico.setId(tecnicoId);
        tecnico.setActivo(true);
        Disponibilidad existente = new Disponibilidad(
                tecnico,
                inicioExistente,
                inicioExistente.plusHours(3)
        );
        existente.setId(4L);
        DisponibilidadRequest request = new DisponibilidadRequest(
                inicioExistente.plusHours(2),
                inicioExistente.plusHours(4)
        );
        when(tecnicoRepository.findByIdForUpdate(tecnicoId)).thenReturn(Optional.of(tecnico));
        when(disponibilidadRepository.findByTecnicoId(tecnicoId))
                .thenReturn(List.of(existente));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> disponibilidadService.crear(tecnicoId, request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(disponibilidadRepository, never()).save(any(Disponibilidad.class));
    }
}
