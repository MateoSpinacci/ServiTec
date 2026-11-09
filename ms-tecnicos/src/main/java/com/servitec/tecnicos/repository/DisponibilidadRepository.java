package com.servitec.tecnicos.repository;

import com.servitec.tecnicos.entity.Disponibilidad;
import com.servitec.tecnicos.entity.Reserva;
import com.servitec.tecnicos.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Long> {
    List<Disponibilidad> findByTecnicoId(Long tecnicoId);
}
