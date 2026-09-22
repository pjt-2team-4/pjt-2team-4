package com.rookies6.myspringboot4project.common.advice;

import com.rookies6.myspringboot4project.sec.project.entity.Project;
import com.rookies6.myspringboot4project.sec.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final ProjectRepository projectRepository;

    // 모든 뷰(View) 컨트롤러가 반환하는 화면에 "projects" 데이터를 자동으로 꽂아줍니다.
    @ModelAttribute("projects")
    public List<Project> populateProjects() {
        return projectRepository.findAll();
    }
}