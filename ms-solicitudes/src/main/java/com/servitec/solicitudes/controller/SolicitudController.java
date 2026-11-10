
package com.servitec.solicitudes.controller;

import com.servitec.solicitudes.entity.Solicitud;
import com.servitec.solicitudes.enums.EstadoSolicitud;
import com.servitec.solicitudes.service.SolicitudService;
import com.servitec.solicitudes.dto.solicitud.request.SolicitudRequest;
import com.servitec.solicitudes.dto.asignacion.request.AsignacionRequest;
import com.servitec.solicitudes.dto.resolucion.request.ResolucionRequest;
import com.servitec.solicitudes.dto.solicitud.response.SolicitudResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Optional;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@RequestMapping("/api/v1/solicitudes")
public class SolicitudController {

    SolicitudService solicitudService;

    private SolicitudResponse convertirRespuesta(Solicitud solicitud) {
        Long tecnicoId = solicitudService.obtenerTecnicoAsignado(solicitud.getId());

        return SolicitudResponse.of(solicitud, tecnicoId);
    }

    @PostMapping
    public ResponseEntity<SolicitudResponse> crear(@Valid @RequestBody SolicitudRequest request,
                                                   @AuthenticationPrincipal Jwt jwt) {
        Solicitud nuevaSolicitud = solicitudService.crear(request, jwt.getSubject());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(convertirRespuesta(nuevaSolicitud));
    }

    @GetMapping
    public ResponseEntity<List<SolicitudResponse>> obtenerTodas(
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication,
            @RequestParam(required = false) EstadoSolicitud estado) {

        List<Solicitud> solicitudes = solicitudService.obtenerTodas(
                jwt.getSubject(),
                authentication.getAuthorities(),
                estado
        );

        List<SolicitudResponse> respuestas = solicitudes.stream()
                .map(this::convertirRespuesta)
                .toList();

        return ResponseEntity.ok(respuestas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitudResponse> obtenerPorId(@PathVariable @Positive Long id,
                                                          @AuthenticationPrincipal Jwt jwt,
                                                          Authentication authentication) {

        Solicitud solicitud = solicitudService.obtenerPorId(id, jwt.getSubject(), authentication.getAuthorities());

        return ResponseEntity.ok(convertirRespuesta(solicitud));
    }

    @PatchMapping("/{id}/cancelacion")
    public ResponseEntity<SolicitudResponse> cancelar(@PathVariable @Positive Long id,
                                                      @AuthenticationPrincipal Jwt jwt) {
        Solicitud solicitudCancelada = solicitudService.cancelar(id, jwt.getSubject());

        return ResponseEntity.ok(convertirRespuesta(solicitudCancelada));
    }

    @PostMapping("/{id}/asignaciones")
    public ResponseEntity<SolicitudResponse> asignarTecnico(@PathVariable @Positive Long id,
                                                            @Valid @RequestBody AsignacionRequest request) {
        Solicitud solicitudAsignada = solicitudService.asignarTecnico(id, request);

        return ResponseEntity.ok(convertirRespuesta(solicitudAsignada));
    }

    @PatchMapping("/{id}/inicio")
    public ResponseEntity<SolicitudResponse> iniciarTrabajo(@PathVariable @Positive Long id,
                                                            @AuthenticationPrincipal Jwt jwt) {
        Solicitud solicitud = solicitudService.iniciarTrabajo(id, jwt.getSubject());

        return ResponseEntity.ok(convertirRespuesta(solicitud));
    }

    @PatchMapping("/{id}/resolucion")
    public ResponseEntity<SolicitudResponse> resolverTrabajo(@PathVariable @Positive Long id,
                                                             @Valid @RequestBody ResolucionRequest request,
                                                             @AuthenticationPrincipal Jwt jwt) {
        Solicitud solicitud = solicitudService.resolverTrabajo(id, jwt.getSubject(), request
        );

        return ResponseEntity.ok(convertirRespuesta(solicitud));
    }
}

