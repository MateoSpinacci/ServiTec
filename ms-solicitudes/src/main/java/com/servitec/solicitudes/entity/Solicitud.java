package com.servitec.solicitudes.entity;

import com.servitec.solicitudes.enums.EstadoSolicitud;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity
@Table(name = "solicitudes")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "cliente_keycloak_id", nullable = false)
    Long clienteId;

    @Column(nullable = false)
    Long especialidadId;

    @Column(nullable = false)
    String descripcion;

    @Column(nullable = false)
    String direccion;

    @Column(nullable = false)
    String latitud;

    @Column(nullable = false)
    Double longitud;

    @Column(name = "fecha_hora_solicitada", nullable = false)
    LocalDateTime fechaSolicitud;

    @Column(name = "duracion_estimada_min", nullable = false)
    Integer duracionEstimada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    EstadoSolicitud estado;

    @Column(nullable = false)
    LocalDateTime fechaCreacion;
}