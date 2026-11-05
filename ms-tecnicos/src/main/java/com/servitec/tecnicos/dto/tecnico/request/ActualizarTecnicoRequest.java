package com.servitec.tecnicos.dto.tecnico.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ActualizarTecnicoRequest {
    String nombre;
    String telefono;
    String direccionBase;
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    Double latitudBase;
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    Double longitudBase;
    @Size(min = 1, message = "Las especialidades no pueden estar vacías")
    Set<Long> especialidadesId;

    @AssertTrue(message = "Debe proporcionar al menos un campo para actualizar")
    public boolean isAlMenosUnCampoPresente() {
        return (nombre != null && !nombre.isBlank())
                || (telefono != null && !telefono.isBlank())
                || (direccionBase != null && !direccionBase.isBlank())
                || latitudBase != null
                || longitudBase != null
                || (especialidadesId != null && !especialidadesId.isEmpty());
    }
}
