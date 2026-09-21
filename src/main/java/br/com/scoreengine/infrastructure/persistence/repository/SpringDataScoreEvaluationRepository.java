package br.com.scoreengine.infrastructure.persistence.repository;

import br.com.scoreengine.infrastructure.persistence.entity.ScoreEvaluationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface SpringDataScoreEvaluationRepository extends JpaRepository<ScoreEvaluationEntity, Long> {

    @Query("""
                SELECT e FROM ScoreEvaluationEntity e
                WHERE e.clientId = :clientId
                  AND e.customerType = :customerType
                  AND e.payloadHash = :payloadHash
                  AND e.calculatedAt >= :validSince
                  AND e.modelVersion = :modelVersion
                ORDER BY e.calculatedAt DESC
                LIMIT 1
            """)
    Optional<ScoreEvaluationEntity> findLatestMatchingEvaluation(
            @Param("clientId") String clientId,
            @Param("customerType") String customerType,
            @Param("payloadHash") String payloadHash,
            @Param("validSince") Instant validSince,
            @Param("modelVersion") String modelVersion);
}