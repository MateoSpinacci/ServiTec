package com.servitec.tecnicos.service;

import com.servitec.tecnicos.dto.especialidad.response.EspecialidadResponse;
import com.servitec.tecnicos.dto.especialidad.request.CrearEspecialidadRequest;
import com.servitec.tecnicos.entity.Especialidad;
import com.servitec.tecnicos.repository.EspecialidadRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class EspecialidadService {
    EspecialidadRepository especialidadRepository;

    public EspecialidadResponse crear(CrearEspecialidadRequest request) {
        if (especialidadRepository.existsByNombre(request.getNombre())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La especialidad ya existe en el sistema."
            );
        }

        Especialidad guardado = especialidadRepository.save(Especialidad.of(request));

        return EspecialidadResponse.of(guardado);
    }

    public List<EspecialidadResponse> listar() {
        return especialidadRepository.findAll()
                .stream()
                .map(EspecialidadResponse::of)
                .toList();
    }
}
