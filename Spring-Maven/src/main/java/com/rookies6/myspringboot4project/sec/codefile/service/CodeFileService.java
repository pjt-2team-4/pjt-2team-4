package com.rookies6.myspringboot4project.sec.codefile.service;

import com.rookies6.myspringboot4project.exception.BusinessException;
import com.rookies6.myspringboot4project.exception.ErrorCode;
import com.rookies6.myspringboot4project.sec.codefile.dto.CodeFileDTO;
import com.rookies6.myspringboot4project.sec.codefile.entity.CodeFile;
import com.rookies6.myspringboot4project.sec.codefile.repository.CodeFileRepository;
import com.rookies6.myspringboot4project.sec.project.entity.Project;
import com.rookies6.myspringboot4project.sec.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodeFileService {

    private final CodeFileRepository codeFileRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public CodeFileDTO.Response createCodeFile(CodeFileDTO.Request request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Project", "id", request.getProjectId()));

        CodeFile codeFile = CodeFile.builder()
                .project(project)
                .fileName(request.getFileName())
                .language(request.getLanguage())
                .sourceCode(request.getSourceCode())
                .build();

        CodeFile savedFile = codeFileRepository.save(codeFile);
        return CodeFileDTO.Response.fromEntity(savedFile);
    }

    public List<CodeFileDTO.Response> getCodeFilesByProject(Long projectId) {
        return codeFileRepository.findByProjectId(projectId).stream()
                .map(CodeFileDTO.Response::fromEntity)
                .toList();
    }

    public CodeFileDTO.Response getCodeFileById(Long id) {
        CodeFile codeFile = codeFileRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "CodeFile", "id", id));
        return CodeFileDTO.Response.fromEntity(codeFile);
    }

    @Transactional
    public CodeFileDTO.Response updateCodeFile(Long id, CodeFileDTO.Request request) {
        CodeFile codeFile = codeFileRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "CodeFile", "id", id));

        codeFile.updateContent(request.getFileName(), request.getSourceCode(), request.getLanguage());
        return CodeFileDTO.Response.fromEntity(codeFile);
    }

    @Transactional
    public void deleteCodeFile(Long id) {
        if (!codeFileRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "CodeFile", "id", id);
        }
        codeFileRepository.deleteById(id);
    }
}