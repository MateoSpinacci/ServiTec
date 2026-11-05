package com.servitec.tecnicos.controller;

import com.servitec.tecnicos.dto.disponibilidad.request.CrearDisponibilidadRequest;
import com.servitec.tecnicos.dto.disponibilidad.response.DisponibilidadResponse;
import com.servitec.tecnicos.dto.tecnico.response.TecnicoResponse;
import com.servitec.tecnicos.service.DisponibilidadService;
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
@RequestMapping("/api/v1/tecnicos/{id}/disponibilidades")
public class DisponibilidadController {
    DisponibilidadService disponibilidadService;

    @PostMapping
    public ResponseEntity<DisponibilidadResponse> crear(@PathVariable Long id,
                                                        @Valid @RequestBody CrearDisponibilidadRequest request) {
        DisponibilidadResponse response = disponibilidadService.crear(id, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<DisponibilidadResponse>> listar(@PathVariable Long id) {
        List<DisponibilidadResponse> response = disponibilidadService.listar(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
