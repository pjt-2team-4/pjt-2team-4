package com.rookies6.myspringboot4project.sec.analysisfile.repository;

import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface AnalysisFileRepository extends JpaRepository<AnalysisFile, Long> {
    @Query("SELECT f FROM AnalysisFile f WHERE f.analysisRequest.id = :analysisId")
    List<AnalysisFile> findByAnalysisRequestId(@Param("analysisId") Long analysisId);

    @Query("SELECT COUNT(f) FROM AnalysisFile f WHERE f.analysisRequest.id = :analysisId")
    long countByAnalysisRequestId(@Param("analysisId") Long analysisId);
}