package com.servitec.tecnicos.repository;

import com.servitec.tecnicos.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    boolean existsByTecnicoIdAndEstadoAndInicioLessThanAndFinGreaterThan(
            Long tecnicoId,
            String estado,
            LocalDateTime fin,
            LocalDateTime inicio
    );
}
