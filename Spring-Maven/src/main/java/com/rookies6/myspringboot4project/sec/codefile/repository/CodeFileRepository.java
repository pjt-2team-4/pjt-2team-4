package com.rookies6.myspringboot4project.sec.codefile.repository;

import com.rookies6.myspringboot4project.sec.codefile.entity.CodeFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodeFileRepository extends JpaRepository<CodeFile, Long> {
    List<CodeFile> findByProjectId(Long projectId);
}