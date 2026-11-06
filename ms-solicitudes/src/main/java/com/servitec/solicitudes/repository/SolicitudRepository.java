package com.servitec.solicitudes.repository;

import com.servitec.solicitudes.entity.Solicitud;
import com.servitec.solicitudes.enums.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    List<Solicitud> findByTecnicoIdAndEstadoIn(
            Long tecnicoId,
            List<EstadoSolicitud> estados
    );
}