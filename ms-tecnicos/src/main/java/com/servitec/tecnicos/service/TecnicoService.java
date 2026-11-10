package com.servitec.tecnicos.service;

import com.servitec.tecnicos.dto.tecnico.response.TecnicoResponse;
import com.servitec.tecnicos.dto.tecnico.request.ActualizarTecnicoRequest;
import com.servitec.tecnicos.dto.tecnico.request.CrearTecnicoRequest;
import com.servitec.tecnicos.entity.Especialidad;
import com.servitec.tecnicos.entity.Tecnico;
import com.servitec.tecnicos.repository.EspecialidadRepository;
import com.servitec.tecnicos.repository.TecnicoRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class TecnicoService {
    TecnicoRepository tecnicoRepository;
    EspecialidadRepository especialidadRepository;

    @Transactional
    public TecnicoResponse crear(CrearTecnicoRequest request) {
        Set<Especialidad> especialidades = new HashSet<>();

        if (tecnicoRepository.existsByKeycloakUserId(request.getKeycloakUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El técnico ya existe en el sistema."
            );
        }
        for (Long idEspecialidad : request.getEspecialidadesId()) {
            Especialidad especialidad = especialidadRepository.findById(idEspecialidad)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Especialidad no existente."
                    ));

            especialidades.add(especialidad);
        }

        Tecnico tecnico = Tecnico.of(request);

        tecnico.setEspecialidades(especialidades);

        Tecnico guardado = tecnicoRepository.save(tecnico);

        return TecnicoResponse.of(guardado);
    }

    public List<TecnicoResponse> listar() {
        return tecnicoRepository.findAll()
                .stream()
                .map(TecnicoResponse::of)
                .toList();
    }

    public TecnicoResponse buscarPorId(Long id) {
        return TecnicoResponse.of(
                tecnicoRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Técnico no encontrado."))
        );
    }

    public TecnicoResponse buscarPorKeycloakUserId(String keycloakUserId) {
        return TecnicoResponse.of(
                tecnicoRepository.findByKeycloakUserId(keycloakUserId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Técnico no encontrado."))
        );
    }

    @Transactional
    public TecnicoResponse actualizar(Long id, ActualizarTecnicoRequest request) {
        Tecnico tecnico = tecnicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Técnico no encontrado."));

        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            tecnico.setNombre(request.getNombre());
        }

        if (request.getTelefono() != null && !request.getTelefono().isBlank()) {
            tecnico.setTelefono(request.getTelefono());
        }

        if (request.getDireccionBase() != null && !request.getDireccionBase().isBlank()) {
            tecnico.setDireccionBase(request.getDireccionBase());
        }

        if (request.getLatitudBase() != null) {
            tecnico.setLatitudBase(request.getLatitudBase());
        }

        if (request.getLongitudBase() != null) {
            tecnico.setLongitudBase(request.getLongitudBase());
        }
        if (request.getEspecialidadesId() != null) {
            Set<Especialidad> especialidades = new HashSet<>();
            for (Long idEspecialidad : request.getEspecialidadesId()) {
                Especialidad especialidad = especialidadRepository.findById(idEspecialidad)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Especialidad no existente."
                        ));

                especialidades.add(especialidad);
            }
            tecnico.setEspecialidades(especialidades);
        }
        return TecnicoResponse.of(tecnicoRepository.save(tecnico));
    }

    @Transactional
    public TecnicoResponse desactivar(Long id) {
        Tecnico tecnico = tecnicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Técnico no encontrado."
                ));

        tecnico.setActivo(false);

        return TecnicoResponse.of(tecnico);
    }
}
