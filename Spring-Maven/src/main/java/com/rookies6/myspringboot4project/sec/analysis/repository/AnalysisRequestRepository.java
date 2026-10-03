
package com.rookies6.myspringboot4project.sec.analysis.repository;

import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisStatus;
import com.rookies6.myspringboot4project.sec.common.enums.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AnalysisRequestRepository extends JpaRepository<AnalysisRequest, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AnalysisRequest a WHERE a.id = :id")
    Optional<AnalysisRequest> findLockedById(@Param("id") Long id);
    long countByStatusAndCreatedAtBetween(AnalysisStatus status, LocalDateTime start, LocalDateTime end);

    long countByOverallSeverity(Severity overallSeverity);

    List<AnalysisRequest> findByStatusAndStartedAtIsNotNullAndCompletedAtIsNotNull(AnalysisStatus status);

    List<AnalysisRequest> findTop5ByOrderByCreatedAtDesc();
}
