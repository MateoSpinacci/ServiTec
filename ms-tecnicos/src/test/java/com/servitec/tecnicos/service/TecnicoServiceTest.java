package com.servitec.tecnicos.service;

// Clases de la aplicación: datos de entrada/salida, entidades y repositorios usados por el servicio.
import com.servitec.tecnicos.dto.tecnico.request.CrearTecnicoRequest;
import com.servitec.tecnicos.dto.tecnico.response.TecnicoResponse;
import com.servitec.tecnicos.entity.Especialidad;
import com.servitec.tecnicos.entity.Tecnico;
import com.servitec.tecnicos.repository.EspecialidadRepository;
import com.servitec.tecnicos.repository.TecnicoRepository;
// JUnit define y ejecuta los casos de prueba.
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
// Mockito reemplaza los repositorios por simulaciones y las inyecta en el servicio.
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
// Tipos de Spring para comprobar el estado HTTP de las excepciones.
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

// Colecciones y Optional de Java para preparar resultados de repositorio simulados.
import java.util.List;
import java.util.Optional;

// Aserciones validan resultados y Mockito configura/verifica las dependencias simuladas.
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TecnicoServiceTest {

    @Mock
    private TecnicoRepository tecnicoRepository;

    @Mock
    private EspecialidadRepository especialidadRepository;

    @InjectMocks
    private TecnicoService tecnicoService;

    // Comprueba que se guarde un técnico activo con la especialidad solicitada.
    @Test
    void crearGuardaTecnicoActivoConSuEspecialidad() {
        CrearTecnicoRequest request = new CrearTecnicoRequest(
                "usuario-1", "Ana", "123456", "Calle 123",
                -34.6, -58.4, List.of(5L)
        );
        Especialidad especialidad = new Especialidad();
        especialidad.setId(5L);
        especialidad.setNombre("Plomería");
        especialidad.setDescripcion("Trabajos de plomería");
        when(tecnicoRepository.existsByKeycloakUserId("usuario-1")).thenReturn(false);
        when(especialidadRepository.findById(5L)).thenReturn(Optional.of(especialidad));
        when(tecnicoRepository.save(any(Tecnico.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TecnicoResponse response = tecnicoService.crear(request);

        assertEquals("usuario-1", response.getKeycloakUserId());
        assertEquals("Ana", response.getNombre());
        assertEquals(true, response.isActivo());
        assertEquals(1, response.getEspecialidades().size());
        assertEquals(5L, response.getEspecialidades().get(0).getId());
        verify(tecnicoRepository).save(any(Tecnico.class));
    }

    // Comprueba que no se permita registrar dos veces el mismo usuario.
    @Test
    void crearFallaSiElTecnicoYaExiste() {
        CrearTecnicoRequest request = new CrearTecnicoRequest(
                "usuario-1", "Ana", "123456", "Calle 123",
                -34.6, -58.4, List.of(5L)
        );
        when(tecnicoRepository.existsByKeycloakUserId("usuario-1")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> tecnicoService.crear(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(tecnicoRepository, never()).save(any(Tecnico.class));
        verifyNoInteractions(especialidadRepository);
    }
}
