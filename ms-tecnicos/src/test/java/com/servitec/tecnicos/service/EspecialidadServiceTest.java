package com.servitec.tecnicos.service;

import com.servitec.tecnicos.dto.especialidad.request.CrearEspecialidadRequest;
import com.servitec.tecnicos.dto.especialidad.response.EspecialidadResponse;
import com.servitec.tecnicos.entity.Especialidad;
import com.servitec.tecnicos.repository.EspecialidadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EspecialidadServiceTest {

    private static final String NOMBRE = "Plomería";
    private static final String DESCRIPCION = "Trabajos de plomería";

    @Mock
    private EspecialidadRepository especialidadRepository;

    @InjectMocks
    private EspecialidadService especialidadService;

    @Test
    void crearGuardaEspecialidad() {
        CrearEspecialidadRequest request = new CrearEspecialidadRequest(NOMBRE, DESCRIPCION);

        when(especialidadRepository.existsByNombre(NOMBRE)).thenReturn(false);
        when(especialidadRepository.save(any(Especialidad.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EspecialidadResponse response = especialidadService.crear(request);

        assertAll(
                () -> assertEquals(NOMBRE, response.getNombre()),
                () -> assertEquals(DESCRIPCION, response.getDescripcion())
        );

        ArgumentCaptor<Especialidad> captor = ArgumentCaptor.forClass(Especialidad.class);
        verify(especialidadRepository).save(captor.capture());
        assertAll(
                () -> assertEquals(NOMBRE, captor.getValue().getNombre()),
                () -> assertEquals(DESCRIPCION, captor.getValue().getDescripcion())
        );
        verify(especialidadRepository).existsByNombre(NOMBRE);
    }

    @Test
    void crearFallaSiElNombreYaExiste() {
        CrearEspecialidadRequest request = new CrearEspecialidadRequest(NOMBRE, DESCRIPCION);
        when(especialidadRepository.existsByNombre(NOMBRE)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> especialidadService.crear(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(especialidadRepository).existsByNombre(NOMBRE);
        verify(especialidadRepository, never()).save(any(Especialidad.class));
    }
}