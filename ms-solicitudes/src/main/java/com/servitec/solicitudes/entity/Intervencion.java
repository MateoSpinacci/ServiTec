package com.servitec.solicitudes.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity
@Table(name = "intervenciones")
public class Intervencion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne
    @JoinColumn(name = "solicitudes_id", nullable = false)
    Solicitud solicitud;

    @OneToOne
    @JoinColumn(name = "asignacion_id", nullable = false)
    Asignacion asignacion;

    @Column(nullable = false)
    LocalDateTime inicio;

    @Column(nullable = false)
    LocalDateTime fin;

    String diagnostico;

    String resolucion;
}