package com.servitec.tecnicos.controller;

import com.servitec.tecnicos.dto.reserva.request.ReservaRequest;
import com.servitec.tecnicos.dto.reserva.response.ReservaResponse;
import com.servitec.tecnicos.service.ReservaService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@RequestMapping("/api/v1/reservas")
public class ReservaController {

    ReservaService reservaService;

    @PostMapping
    public ResponseEntity<ReservaResponse> enviarReserva(@Valid @RequestBody ReservaRequest reservaRequest) {
        ReservaResponse response = reservaService.crear(reservaRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/{id}/liberacion")
    public ResponseEntity<Void> liberarReserva(@PathVariable Long id) {
        reservaService.liberarReserva(id);

        return ResponseEntity.noContent().build();
    }
}
