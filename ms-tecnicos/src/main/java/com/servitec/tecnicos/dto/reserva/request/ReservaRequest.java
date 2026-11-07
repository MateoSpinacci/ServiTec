package com.servitec.tecnicos.dto.reserva.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReservaRequest {
    @Positive
    @NotNull
    Long tecnicoId;

    @NotNull
    @Positive
    Long solicitudId;

    @NotNull
    @Future
    LocalDateTime inicio;

    @Future
    @NotNull
    LocalDateTime fin;
}
