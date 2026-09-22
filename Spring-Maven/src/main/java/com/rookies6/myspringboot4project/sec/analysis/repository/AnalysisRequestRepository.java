package com.rookies6.myspringboot4project.sec.analysis.repository;

import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRequestRepository extends JpaRepository<AnalysisRequest, Long> {
}