package com.servitec.tecnicos.controller;

import com.servitec.tecnicos.dto.tecnico.request.CandidatosRequest;
import com.servitec.tecnicos.dto.tecnico.response.CandidatoResponse;
import com.servitec.tecnicos.service.CandidatosService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@RequestMapping("/api/v1/tecnicos/candidatos")
public class CandidatosController {

    private final CandidatosService candidatosService;

    @PostMapping
    public ResponseEntity<List<CandidatoResponse>> buscarCandidatos(@Valid @RequestBody CandidatosRequest request) {
        List<CandidatoResponse> candidatos = candidatosService.buscarCandidatos(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(candidatos);
    }
}
