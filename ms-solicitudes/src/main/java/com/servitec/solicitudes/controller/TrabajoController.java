package com.servitec.solicitudes.controller;

import com.servitec.solicitudes.entity.Solicitud;
import com.servitec.solicitudes.service.SolicitudService;
import com.servitec.solicitudes.dto.solicitud.response.SolicitudResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trabajos")
public class TrabajoController {

    private final SolicitudService solicitudService;

    public TrabajoController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @GetMapping
    public ResponseEntity<List<SolicitudResponse>> obtenerTrabajosDelTecnico() {

        Long tecnicoId = 1L; // Temporal, luego lo obtenemos del JWT.

        List<Solicitud> trabajos =
                solicitudService.obtenerTrabajosDelTecnico(tecnicoId);

        List<SolicitudResponse> respuestas = trabajos.stream()
                .map(SolicitudResponse::of)
                .toList();

        return ResponseEntity.ok(respuestas);
    }
}
