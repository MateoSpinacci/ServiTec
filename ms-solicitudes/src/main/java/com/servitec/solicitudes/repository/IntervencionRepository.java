package com.servitec.solicitudes.repository;

import com.servitec.solicitudes.entity.Intervencion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IntervencionRepository extends JpaRepository<Intervencion, Long> {

    Optional<Intervencion> findBySolicitudId(Long solicitudId);

}
