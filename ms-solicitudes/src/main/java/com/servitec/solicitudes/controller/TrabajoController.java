package com.servitec.solicitudes.controller;

import com.servitec.solicitudes.entity.Solicitud;
import com.servitec.solicitudes.service.SolicitudService;
import com.servitec.solicitudes.dto.solicitud.response.SolicitudResponse;
import com.servitec.solicitudes.service.TrabajoService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@RequestMapping("/api/v1/trabajos")
public class TrabajoController {
    SolicitudService solicitudService;
    TrabajoService trabajoService;

    @GetMapping
    public ResponseEntity<List<SolicitudResponse>> obtenerTrabajosDelTecnico(@AuthenticationPrincipal Jwt jwt) {

        List<Solicitud> trabajos = trabajoService.obtenerTrabajosDelTecnico(jwt.getSubject());

        List<SolicitudResponse> respuestas = trabajos.stream()
                .map(solicitud -> SolicitudResponse.of(
                        solicitud,
                        solicitudService.obtenerTecnicoAsignado(solicitud.getId())
                ))
                .toList();

        return ResponseEntity.ok(respuestas);
    }
}
