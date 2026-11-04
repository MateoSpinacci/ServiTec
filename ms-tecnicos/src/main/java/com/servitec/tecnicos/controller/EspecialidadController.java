package com.servitec.tecnicos.controller;


import com.servitec.tecnicos.dto.reponse.EspecialidadResponse;
import com.servitec.tecnicos.dto.request.CrearEspecialidadRequest;
import com.servitec.tecnicos.service.EspecialidadService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@RequestMapping("/api/v1/especialidades")
public class EspecialidadController {

    EspecialidadService especialidadService;

    @PostMapping
    public ResponseEntity<EspecialidadResponse> crear(@Valid @RequestBody CrearEspecialidadRequest request) {
        EspecialidadResponse response = especialidadService.crear(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<EspecialidadResponse>> listar() {
        List<EspecialidadResponse> response = especialidadService.listar();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
