package com.servitec.tecnicos.dto.tecnico.request;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CrearTecnicoRequest {
    @NotBlank
    String keycloakUserId;

    @NotBlank
    String nombre;

    String telefono;

    @NotBlank
    String direccionBase;

    @NotNull
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    Double latitudBase;

    @NotNull
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    Double longitudBase;

    @NotEmpty
    List<@NotNull Long> especialidadesId;
}
