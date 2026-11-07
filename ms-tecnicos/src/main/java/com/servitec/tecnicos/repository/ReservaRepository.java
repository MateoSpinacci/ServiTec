package com.servitec.tecnicos.repository;

import com.servitec.tecnicos.entity.Reserva;
import com.servitec.tecnicos.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    boolean existsByTecnicoIdAndEstadoAndInicioLessThanAndFinGreaterThan(
            Long tecnicoId,
            String estado,
            LocalDateTime fin,
            LocalDateTime inicio
    );

    boolean existsBySolicitudIdAndEstado(
            Long solicitudId,
            EstadoReserva estado
    );

    @Query("""
        SELECT COUNT(r) > 0
        FROM Reserva r
        WHERE r.tecnico.id = :tecnicoId
          AND r.estado = :estado
          AND r.inicio < :fin
          AND r.fin > :inicio
        """)
    boolean existeSuperposicion(
            @Param("tecnicoId") Long tecnicoId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("estado") EstadoReserva estado
    );
}
