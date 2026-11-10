package com.servitec.solicitudes.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
    @JoinColumn(name = "solicitud_id", nullable = false, unique = true)
    Solicitud solicitud;

    @OneToOne
    @JoinColumn(name = "asignacion_id", nullable = false)
    Asignacion asignacion;

    @Column(nullable = false)
    LocalDateTime inicio;

    LocalDateTime fin;

    String diagnostico;

    String resolucion;
}