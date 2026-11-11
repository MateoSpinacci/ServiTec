package com.servitec.tecnicos.service;

import com.servitec.tecnicos.dto.tecnico.request.ActualizarTecnicoRequest;
import com.servitec.tecnicos.dto.tecnico.request.CrearTecnicoRequest;
import com.servitec.tecnicos.dto.tecnico.response.TecnicoResponse;
import com.servitec.tecnicos.entity.Especialidad;
import com.servitec.tecnicos.entity.Tecnico;
import com.servitec.tecnicos.repository.EspecialidadRepository;
import com.servitec.tecnicos.repository.TecnicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TecnicoServiceTest {
    private static final Long TECNICO_ID = 12L;
    private static final Long ESPECIALIDAD_ID = 5L;
    private static final Long OTRA_ESPECIALIDAD_ID = 6L;
    private static final String KEYCLOAK_ID = "550e8400-e29b-41d4-a716-446655440002";

    @Mock TecnicoRepository tecnicoRepository;
    @Mock EspecialidadRepository especialidadRepository;
    @InjectMocks TecnicoService tecnicoService;

    @Test
    void crearPersisteDatosCompletosYEspecialidad() {
        Especialidad especialidad = especialidad(ESPECIALIDAD_ID);
        when(especialidadRepository.findById(ESPECIALIDAD_ID)).thenReturn(Optional.of(especialidad));
        when(tecnicoRepository.save(any(Tecnico.class))).thenAnswer(inv -> inv.getArgument(0));

        TecnicoResponse respuesta = tecnicoService.crear(crearRequest(List.of(ESPECIALIDAD_ID)));

        ArgumentCaptor<Tecnico> captor = ArgumentCaptor.forClass(Tecnico.class);
        verify(tecnicoRepository).save(captor.capture());
        Tecnico guardado = captor.getValue();
        assertAll(
                () -> assertEquals(KEYCLOAK_ID, guardado.getKeycloakUserId()),
                () -> assertEquals("Ana", guardado.getNombre()),
                () -> assertEquals("123456", guardado.getTelefono()),
                () -> assertEquals("Calle 123", guardado.getDireccionBase()),
                () -> assertEquals(-34.6, guardado.getLatitudBase()),
                () -> assertEquals(-58.4, guardado.getLongitudBase()),
                () -> assertTrue(guardado.isActivo()),
                () -> assertEquals(Set.of(especialidad), guardado.getEspecialidades()),
                () -> assertEquals(KEYCLOAK_ID, respuesta.getKeycloakUserId()),
                () -> assertTrue(respuesta.isActivo()),
                () -> assertEquals(1, respuesta.getEspecialidades().size())
        );
    }

    @Test
    void crearAsociaVariasEspecialidades() {
        when(especialidadRepository.findById(ESPECIALIDAD_ID)).thenReturn(Optional.of(especialidad(ESPECIALIDAD_ID)));
        when(especialidadRepository.findById(OTRA_ESPECIALIDAD_ID)).thenReturn(Optional.of(especialidad(OTRA_ESPECIALIDAD_ID)));
        when(tecnicoRepository.save(any(Tecnico.class))).thenAnswer(inv -> inv.getArgument(0));

        TecnicoResponse respuesta = tecnicoService.crear(crearRequest(List.of(ESPECIALIDAD_ID, OTRA_ESPECIALIDAD_ID)));

        assertEquals(Set.of(ESPECIALIDAD_ID, OTRA_ESPECIALIDAD_ID),
                respuesta.getEspecialidades().stream().map(e -> e.getId()).collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void crearRechazaUsuarioDuplicado() {
        when(tecnicoRepository.existsByKeycloakUserId(KEYCLOAK_ID)).thenReturn(true);
        assertEstado(HttpStatus.CONFLICT, () -> tecnicoService.crear(crearRequest(List.of(ESPECIALIDAD_ID))));
        verify(tecnicoRepository, never()).save(any());
        verifyNoInteractions(especialidadRepository);
    }

    @Test
    void crearRechazaEspecialidadInexistenteSinGuardar() {
        when(especialidadRepository.findById(ESPECIALIDAD_ID)).thenReturn(Optional.empty());
        assertEstado(HttpStatus.NOT_FOUND, () -> tecnicoService.crear(crearRequest(List.of(ESPECIALIDAD_ID))));
        verify(tecnicoRepository, never()).save(any());
    }

    @Test
    void buscarPorIdDevuelveTecnico() {
        when(tecnicoRepository.findById(TECNICO_ID)).thenReturn(Optional.of(tecnico()));
        TecnicoResponse respuesta = tecnicoService.buscarPorId(TECNICO_ID);
        assertEquals(KEYCLOAK_ID, respuesta.getKeycloakUserId());
    }

    @Test
    void buscarPorIdInexistenteDevuelve404() {
        when(tecnicoRepository.findById(TECNICO_ID)).thenReturn(Optional.empty());
        assertEstado(HttpStatus.NOT_FOUND, () -> tecnicoService.buscarPorId(TECNICO_ID));
    }

    @Test
    void buscarPorKeycloakIdInexistenteDevuelve404() {
        when(tecnicoRepository.findByKeycloakUserId(KEYCLOAK_ID)).thenReturn(Optional.empty());
        assertEstado(HttpStatus.NOT_FOUND, () -> tecnicoService.buscarPorKeycloakUserId(KEYCLOAK_ID));
    }

    @Test
    void actualizarModificaSoloCamposInformados() {
        Tecnico tecnico = tecnico();
        when(tecnicoRepository.findById(TECNICO_ID)).thenReturn(Optional.of(tecnico));
        when(tecnicoRepository.save(tecnico)).thenReturn(tecnico);
        ActualizarTecnicoRequest request = new ActualizarTecnicoRequest("Nuevo nombre", null, null, null, null, null);

        TecnicoResponse respuesta = tecnicoService.actualizar(TECNICO_ID, request);

        assertAll(
                () -> assertEquals("Nuevo nombre", respuesta.getNombre()),
                () -> assertEquals("123456", tecnico.getTelefono()),
                () -> assertEquals("Calle 123", tecnico.getDireccionBase()),
                () -> assertEquals(KEYCLOAK_ID, tecnico.getKeycloakUserId())
        );
        verifyNoInteractions(especialidadRepository);
    }

    @Test
    void actualizarReemplazaEspecialidades() {
        Tecnico tecnico = tecnico();
        tecnico.setEspecialidades(new java.util.HashSet<>(Set.of(especialidad(ESPECIALIDAD_ID))));
        Especialidad nueva = especialidad(OTRA_ESPECIALIDAD_ID);
        when(tecnicoRepository.findById(TECNICO_ID)).thenReturn(Optional.of(tecnico));
        when(especialidadRepository.findById(OTRA_ESPECIALIDAD_ID)).thenReturn(Optional.of(nueva));
        when(tecnicoRepository.save(tecnico)).thenReturn(tecnico);

        tecnicoService.actualizar(TECNICO_ID, new ActualizarTecnicoRequest(null, null, null, null, null, Set.of(OTRA_ESPECIALIDAD_ID)));

        assertEquals(Set.of(nueva), tecnico.getEspecialidades());
    }

    @Test
    void actualizarRechazaEspecialidadInexistenteSinGuardar() {
        when(tecnicoRepository.findById(TECNICO_ID)).thenReturn(Optional.of(tecnico()));
        when(especialidadRepository.findById(OTRA_ESPECIALIDAD_ID)).thenReturn(Optional.empty());
        assertEstado(HttpStatus.NOT_FOUND, () -> tecnicoService.actualizar(TECNICO_ID,
                new ActualizarTecnicoRequest(null, null, null, null, null, Set.of(OTRA_ESPECIALIDAD_ID))));
        verify(tecnicoRepository, never()).save(any());
    }

    @Test
    void desactivarMarcaTecnicoComoInactivo() {
        Tecnico tecnico = tecnico();
        when(tecnicoRepository.findById(TECNICO_ID)).thenReturn(Optional.of(tecnico));
        TecnicoResponse respuesta = tecnicoService.desactivar(TECNICO_ID);
        assertFalse(respuesta.isActivo());
        assertFalse(tecnico.isActivo());
    }

    @Test
    void desactivarTecnicoInexistenteDevuelve404() {
        when(tecnicoRepository.findById(TECNICO_ID)).thenReturn(Optional.empty());
        assertEstado(HttpStatus.NOT_FOUND, () -> tecnicoService.desactivar(TECNICO_ID));
    }

    private CrearTecnicoRequest crearRequest(List<Long> ids) {
        return new CrearTecnicoRequest(KEYCLOAK_ID, "Ana", "123456", "Calle 123", -34.6, -58.4, ids);
    }

    private Tecnico tecnico() {
        Tecnico tecnico = Tecnico.of(crearRequest(List.of(ESPECIALIDAD_ID)));
        tecnico.setId(TECNICO_ID);
        return tecnico;
    }

    private Especialidad especialidad(Long id) {
        Especialidad especialidad = new Especialidad();
        especialidad.setId(id);
        especialidad.setNombre("Especialidad " + id);
        return especialidad;
    }

    private void assertEstado(HttpStatus estado, org.junit.jupiter.api.function.Executable operacion) {
        assertEquals(estado, assertThrows(ResponseStatusException.class, operacion).getStatusCode());
    }
}

