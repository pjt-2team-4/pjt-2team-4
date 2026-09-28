package com.rookies6.myspringboot4project.sec.projectfile.repository;

import com.rookies6.myspringboot4project.sec.projectfile.entity.ProjectFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectFileRepository extends JpaRepository<ProjectFile, Long> {
    List<ProjectFile> findByProjectId(Long projectId);
}