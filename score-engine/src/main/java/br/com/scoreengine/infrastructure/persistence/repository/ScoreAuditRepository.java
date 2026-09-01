package br.com.scoreengine.infrastructure.persistence.repository;

import br.com.scoreengine.infrastructure.persistence.entity.ScoreAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ScoreAuditRepository extends JpaRepository<ScoreAuditEntity, UUID> {
}