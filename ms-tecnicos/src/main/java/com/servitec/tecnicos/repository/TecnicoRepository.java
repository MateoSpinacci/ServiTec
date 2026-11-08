package com.servitec.tecnicos.repository;

import com.servitec.tecnicos.entity.Tecnico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {
    boolean existsByKeycloakUserId(String keycloakUserId);
    Optional<Tecnico> findByKeycloakUserId(String keycloakUserId);
}
