
package com.rookies6.myspringboot4project.sec.analysis.repository;

import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisStatus;
import com.rookies6.myspringboot4project.sec.common.enums.Severity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AnalysisRequestRepository extends JpaRepository<AnalysisRequest, Long> {
    long countByStatusAndCreatedAtBetween(AnalysisStatus status, LocalDateTime start, LocalDateTime end);

    long countByOverallSeverity(Severity overallSeverity);

    List<AnalysisRequest> findByStatusAndStartedAtIsNotNullAndCompletedAtIsNotNull(AnalysisStatus status);

    List<AnalysisRequest> findTop5ByOrderByCreatedAtDesc();
}