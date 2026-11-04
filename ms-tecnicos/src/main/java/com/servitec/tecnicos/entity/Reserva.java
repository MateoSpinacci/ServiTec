package com.servitec.tecnicos.entity;

import com.servitec.tecnicos.enums.EstadoReserva;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity
@Table(name = "reserva")
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "tecnico_id", nullable = false)
    Tecnico tecnico;

    @Column(name = "solicitud_id", nullable = false)
    Long solicitudId;

    @Column(nullable = false)
    LocalDateTime inicio;

    @Column(nullable = false)
    LocalDateTime fin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    EstadoReserva estado;
}
