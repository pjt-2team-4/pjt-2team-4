package com.rookies6.myspringboot4project.sec.project.service;

import com.rookies6.myspringboot4project.exception.BusinessException;
import com.rookies6.myspringboot4project.exception.ErrorCode;
import com.rookies6.myspringboot4project.sec.project.dto.ProjectDTO;
import com.rookies6.myspringboot4project.sec.project.entity.Project;
import com.rookies6.myspringboot4project.sec.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Transactional
    public ProjectDTO.Response createProject(ProjectDTO.Request request) {
        Project project = request.toEntity();
        Project savedProject = projectRepository.save(project);
        return ProjectDTO.Response.fromEntity(savedProject);
    }

    public List<ProjectDTO.Response> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(ProjectDTO.Response::fromEntity)
                .toList();
    }

    public ProjectDTO.Response getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Project", "id", id));
        return ProjectDTO.Response.fromEntity(project);
    }

    @Transactional
    public ProjectDTO.Response updateProject(Long id, ProjectDTO.Request request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Project", "id", id));
        
        project.update(request.getName(), request.getDescription(), request.getLanguage());
        return ProjectDTO.Response.fromEntity(project);
    }

    @Transactional
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Project", "id", id);
        }
        projectRepository.deleteById(id);
    }
}