
package com.servitec.solicitudes.controller;

import com.servitec.solicitudes.entity.Solicitud;
import com.servitec.solicitudes.service.SolicitudService;
import com.servitec.solicitudes.dto.solicitud.request.SolicitudRequest;
import com.servitec.solicitudes.dto.asignacion.request.AsignacionRequest;
import com.servitec.solicitudes.dto.resolucion.request.ResolucionRequest;
import com.servitec.solicitudes.dto.solicitud.response.SolicitudResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/solicitudes")
public class SolicitudController {

    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @PostMapping
    public ResponseEntity<SolicitudResponse> crear(
            @Valid @RequestBody SolicitudRequest request) {

        Long clienteId = 1L;

        Solicitud nuevaSolicitud = solicitudService.crear(request, clienteId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SolicitudResponse.of(nuevaSolicitud));
    }

    @GetMapping
    public ResponseEntity<List<SolicitudResponse>> obtenerTodas() {

        List<Solicitud> solicitudes = solicitudService.obtenerTodas();

        List<SolicitudResponse> respuestas = solicitudes.stream()
                .map(SolicitudResponse::of)
                .toList();

        return ResponseEntity.ok(respuestas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitudResponse> obtenerPorId(
            @PathVariable @Positive Long id) {

        Optional<Solicitud> solicitud = solicitudService.obtenerPorId(id);

        return solicitud
                .map(SolicitudResponse::of)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/cancelacion")
    public ResponseEntity<SolicitudResponse> cancelar(@PathVariable @Positive Long id) {

        Long clienteId = 1L;

        Solicitud solicitudCancelada = solicitudService.cancelar(id, clienteId);

        return ResponseEntity.ok(SolicitudResponse.of(solicitudCancelada));
    }

    @PostMapping("/{id}/asignaciones")
    public ResponseEntity<SolicitudResponse> asignarTecnico(
            @PathVariable @Positive Long id,
            @Valid @RequestBody AsignacionRequest request) {

        Solicitud solicitudAsignada = solicitudService.asignarTecnico(id, request);

        return ResponseEntity.ok(SolicitudResponse.of(solicitudAsignada));
    }

    @PatchMapping("/{id}/inicio")
    public ResponseEntity<SolicitudResponse> iniciarTrabajo(@PathVariable @Positive Long id) {

        Long tecnicoId = 1L;

        Solicitud solicitud = solicitudService.iniciarTrabajo(id, tecnicoId);

        return ResponseEntity.ok(SolicitudResponse.of(solicitud));
    }

    @PatchMapping("/{id}/resolucion")
    public ResponseEntity<SolicitudResponse> resolverTrabajo(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ResolucionRequest request) {

        Long tecnicoId = 1L;

        Solicitud solicitud = solicitudService.resolverTrabajo(
                id,
                tecnicoId,
                request
        );

        return ResponseEntity.ok(SolicitudResponse.of(solicitud));
    }
}

