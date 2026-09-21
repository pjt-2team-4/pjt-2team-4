package com.rookies6.myspringboot4project.sec.repository;

import com.rookies6.myspringboot4project.sec.entity.Analysis;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
    @EntityGraph(attributePaths = {"vulnerabilities"})
    Optional<Analysis> findById(Long id);
}