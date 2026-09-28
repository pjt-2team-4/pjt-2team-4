package com.rookies6.myspringboot4project.sec.projectfile.service;

import com.rookies6.myspringboot4project.exception.BusinessException;
import com.rookies6.myspringboot4project.exception.ErrorCode;
import com.rookies6.myspringboot4project.sec.projectfile.dto.ProjectFileDTO;
import com.rookies6.myspringboot4project.sec.projectfile.entity.ProjectFile;
import com.rookies6.myspringboot4project.sec.projectfile.repository.ProjectFileRepository;
import com.rookies6.myspringboot4project.sec.project.entity.Project;
import com.rookies6.myspringboot4project.sec.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectFileService {

    private final ProjectFileRepository projectFileRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public ProjectFileDTO.Response createProjectFile(ProjectFileDTO.Request request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Project", "id", request.getProjectId()));

        ProjectFile projectFile = ProjectFile.builder()
                .project(project)
                .filePath(request.getFilePath())
                .fileName(request.getFileName())
                .language(request.getLanguage())
                .content(request.getContent())
                .fileSizeBytes(request.getFileSizeBytes())
                .build();

        ProjectFile savedFile = projectFileRepository.save(projectFile);
        return ProjectFileDTO.Response.fromEntity(savedFile);
    }

    public List<ProjectFileDTO.Response> getProjectFilesByProject(Long projectId) {
        return projectFileRepository.findByProjectId(projectId).stream()
                .map(ProjectFileDTO.Response::fromEntity)
                .toList();
    }

    public ProjectFileDTO.Response getProjectFileById(Long id) {
        ProjectFile projectFile = projectFileRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "ProjectFile", "id", id));
        return ProjectFileDTO.Response.fromEntity(projectFile);
    }

    @Transactional
    public ProjectFileDTO.Response updateProjectFile(Long id, ProjectFileDTO.Request request) {
        ProjectFile projectFile = projectFileRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "ProjectFile", "id", id));

        projectFile.updateContent(request.getFilePath(), request.getFileName(), request.getContent(), request.getLanguage(), request.getFileSizeBytes());
        return ProjectFileDTO.Response.fromEntity(projectFile);
    }

    @Transactional
    public void deleteProjectFile(Long id) {
        if (!projectFileRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "ProjectFile", "id", id);
        }
        projectFileRepository.deleteById(id);
    }
}