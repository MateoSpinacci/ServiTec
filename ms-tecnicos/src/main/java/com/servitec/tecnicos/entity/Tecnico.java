package com.servitec.tecnicos.entity;

import com.servitec.tecnicos.dto.tecnico.request.CrearTecnicoRequest;
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

    private Tecnico(String keycloakUserId, String nombre, String telefono, String direccionBase, Double latitudBase, Double longitudBase) {
        this.keycloakUserId = keycloakUserId;
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccionBase = direccionBase;
        this.latitudBase = latitudBase;
        this.longitudBase = longitudBase;
        this.activo = true;
    }

    public static Tecnico of(CrearTecnicoRequest request) {
        return new Tecnico(request.getKeycloakUserId(),
                request.getNombre(),
                request.getTelefono(),
                request.getDireccionBase(),
                request.getLatitudBase(),
                request.getLongitudBase()
                );
    }
}

