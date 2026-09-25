package br.com.scoreengine.infrastructure.persistence.repository;

import br.com.scoreengine.infrastructure.persistence.entity.ScoreAuditTrailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataScoreAuditRepository extends JpaRepository<ScoreAuditTrailEntity, Long> {
}