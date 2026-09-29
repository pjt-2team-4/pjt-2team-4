package com.rookies6.myspringboot4project.sec.project.repository;

import com.rookies6.myspringboot4project.sec.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}