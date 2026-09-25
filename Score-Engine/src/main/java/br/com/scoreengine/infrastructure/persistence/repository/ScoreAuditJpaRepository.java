package br.com.scoreengine.infrastructure.persistence.repository;

import br.com.scoreengine.infrastructure.persistence.entity.ScoreAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositório Spring Data JPA para persistência dos dados de auditoria do
 * Score.
 */
@Repository
public interface ScoreAuditJpaRepository extends JpaRepository<ScoreAuditEntity, UUID> {
}