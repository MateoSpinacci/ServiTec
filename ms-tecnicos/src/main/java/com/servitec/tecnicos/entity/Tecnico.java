package com.servitec.tecnicos.entity;

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
@Table(name = "tecnico")
public class Tecnico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "keycloak_user_id", unique = true, nullable = false)
    String keycloakUserId;

    @Column(nullable = false)
    String nombre;

    String telefono;

    @Column(name = "direccion_base", nullable = false)
    String direccionBase;

    @Column(name = "latitud_base", nullable = false)
    Double latitudBase;

    @Column(name = "longitud_base", nullable = false)
    Double longitudBase;

    @Column(nullable = false)
    boolean activo;

    @ManyToMany
    @JoinTable(
            name = "tecnico_especialidad",
            joinColumns = @JoinColumn(name = "tecnico_id"),
            inverseJoinColumns = @JoinColumn(name = "especialidad_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"tecnico_id", "especialidad_id"})
    )
    private Set<Especialidad> especialidades = new HashSet<>();
}

