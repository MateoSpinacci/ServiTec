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

    @Column(nullable = false, unique = true)
    Long solicitudId;

    @Column(nullable = false)
    Long tecnicoId;

    String diagnostico;

    String resolucion;

    @Column(nullable = false)
    LocalDateTime fechaInicio;

    LocalDateTime fechaResolucion;
}