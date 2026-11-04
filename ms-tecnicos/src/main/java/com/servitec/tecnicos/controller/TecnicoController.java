package com.servitec.tecnicos.controller;

import com.servitec.tecnicos.dto.reponse.TecnicoResponse;
import com.servitec.tecnicos.dto.request.CrearTecnicoRequest;
import com.servitec.tecnicos.service.TecnicoService;
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
@RequestMapping("/api/v1/tecnicos")
public class TecnicoController {

    TecnicoService tecnicoService;

    @PostMapping
    public ResponseEntity<TecnicoResponse> crear(@Valid @RequestBody CrearTecnicoRequest request) {
        TecnicoResponse response = tecnicoService.crear(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<TecnicoResponse>> listar() {
        List<TecnicoResponse> response = tecnicoService.listar();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TecnicoResponse> buscarPorId(@PathVariable Long id) {
        TecnicoResponse response = tecnicoService.buscarPorId(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}


