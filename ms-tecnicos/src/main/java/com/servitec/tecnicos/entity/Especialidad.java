package com.servitec.tecnicos.entity;

import com.servitec.tecnicos.dto.request.CrearEspecialidadRequest;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity
@Table(name = "especialidad")
public class Especialidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true, length = 100)
    String nombre;

    @Column(nullable = false, length = 100)
    String descripcion;

    @ManyToMany(mappedBy = "especialidades")
    private Set<Tecnico> tecnicos = new HashSet<>();

    private Especialidad(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public static Especialidad of(CrearEspecialidadRequest request) {
        return new Especialidad(request.getNombre(), request.getDescripcion());
    }
}
