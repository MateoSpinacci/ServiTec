package com.servitec.solicitudes.repository;

import com.servitec.solicitudes.entity.Asignacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AsignacionRepository extends JpaRepository<Asignacion, Long> {

    Optional<Asignacion> findBySolicitudId(Long solicitudId);

    boolean existsBySolicitudId(Long solicitudId);
}