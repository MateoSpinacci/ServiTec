package com.servitec.solicitudes.repository;

import com.servitec.solicitudes.entity.Solicitud;
import com.servitec.solicitudes.enums.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    @Query("""
            SELECT s
            FROM Solicitud s
            JOIN Asignacion a ON a.solicitud = s
            WHERE a.tecnicoId = :tecnicoId
            AND s.estado IN :estados
            """)
    List<Solicitud> findByTecnicoIdAndEstadoIn(
            @Param("tecnicoId") Long tecnicoId,
            @Param("estados") List<EstadoSolicitud> estados
    );
}