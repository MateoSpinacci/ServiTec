package com.servitec.tecnicos.service;

// Clases de la aplicación: datos de entrada/salida, entidad y repositorio del servicio.
import com.servitec.tecnicos.dto.especialidad.request.CrearEspecialidadRequest;
import com.servitec.tecnicos.dto.especialidad.response.EspecialidadResponse;
import com.servitec.tecnicos.entity.Especialidad;
import com.servitec.tecnicos.repository.EspecialidadRepository;
// JUnit define y ejecuta los casos de prueba.
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
// Mockito simula el repositorio y lo inyecta en el servicio.
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
// Tipos de Spring para comprobar el estado HTTP de las excepciones.
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

// Aserciones validan resultados y Mockito configura/verifica el repositorio simulado.
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EspecialidadServiceTest {

    @Mock
    private EspecialidadRepository especialidadRepository;

    @InjectMocks
    private EspecialidadService especialidadService;

    // Comprueba que una especialidad nueva se guarde y se devuelva con sus datos.
    @Test
    void crearGuardaEspecialidad() {
        CrearEspecialidadRequest request =
                new CrearEspecialidadRequest("Plomería", "Trabajos de plomería");
        when(especialidadRepository.existsByNombre("Plomería")).thenReturn(false);
        when(especialidadRepository.save(any(Especialidad.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EspecialidadResponse response = especialidadService.crear(request);

        assertEquals("Plomería", response.getNombre());
        assertEquals("Trabajos de plomería", response.getDescripcion());
        verify(especialidadRepository).save(any(Especialidad.class));
    }

    // Comprueba que no se permita crear una especialidad con un nombre repetido.
    @Test
    void crearFallaSiElNombreYaExiste() {
        CrearEspecialidadRequest request =
                new CrearEspecialidadRequest("Plomería", "Trabajos de plomería");
        when(especialidadRepository.existsByNombre("Plomería")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> especialidadService.crear(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(especialidadRepository, never()).save(any(Especialidad.class));
    }
}
