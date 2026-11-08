package com.servitec.tecnicos.repository;

import com.servitec.tecnicos.entity.Tecnico;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.util.List;
import java.util.Optional;

@Repository
public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {
    boolean existsByKeycloakUserId(String keycloakUserId);
<<<<<<< HEAD

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Tecnico t WHERE t.id = :id")
    Optional<Tecnico> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            SELECT t FROM Tecnico t
            JOIN t.especialidades e
            WHERE t.activo = true
            AND e.id = :especialidadId
            """)

    List<Tecnico> buscarActivosPorEspecialidad(
            @Param("especialidadId") Long especialidadId
    );
=======
    Optional<Tecnico> findByKeycloakUserId(String keycloakUserId);
>>>>>>> origin/main
}
