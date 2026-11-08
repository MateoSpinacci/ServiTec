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
@Table(name = "asignacion")
public class Asignacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne
    @JoinColumn(name = "solicitudes_id", nullable = false)
    Solicitud solicitud;

    @Column(name = "tecnico_id", nullable = false)
    Long tecnicoId;

    @Column(name = "reserva_id", nullable = false)
    Long reservaId;

    @Column(nullable = false)
    LocalDateTime fechaCreacion;
}
