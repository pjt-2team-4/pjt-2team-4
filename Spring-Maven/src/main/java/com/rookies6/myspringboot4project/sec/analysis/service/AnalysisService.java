package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.exception.BusinessException;
import com.rookies6.myspringboot4project.exception.ErrorCode;
import com.rookies6.myspringboot4project.sec.analysis.dto.AnalysisDTO;
import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.analysis.entity.VulnerabilityFinding;
import com.rookies6.myspringboot4project.sec.analysis.repository.AnalysisRequestRepository;
import com.rookies6.myspringboot4project.sec.project.entity.Project;
import com.rookies6.myspringboot4project.sec.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisService {

    private final AnalysisRequestRepository analysisRequestRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public AnalysisDTO.Response analyzeCode(AnalysisDTO.Request request) {
        // 1. 프로젝트 유효성 검증
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Project", "id", request.getProjectId()));

        // 2. 분석 요청 엔티티 생성 (User 객체는 SecurityContext 등에서 가져와야 할 수 있으나 일단 기존 로직 유지)
        AnalysisRequest analysisRequest = AnalysisRequest.builder()
                .project(project)
                .language(request.getLanguage())
                .sourceCode(request.getCode())
                .build();

        // 3. [추후 LLM 연동 영역] 임시 시연용 취약점 탐지 결과 주입 (새 스키마 적용)
        VulnerabilityFinding sampleFinding = VulnerabilityFinding.builder()
                .ruleId(1L) // 임시 룰 ID
                .severity("HIGH")
                .startLine(1)
                .endLine(5)
                .codeSnippet(request.getCode())
                .build();

        analysisRequest.addFinding(sampleFinding);
        analysisRequest.markAsCompleted();

        // 4. DB 저장
        AnalysisRequest savedRequest = analysisRequestRepository.save(analysisRequest);

        return AnalysisDTO.Response.fromEntity(savedRequest);
    }

    public AnalysisDTO.Response getAnalysisById(Long id) {
        AnalysisRequest analysisRequest = analysisRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "AnalysisRequest", "id", id));
        return AnalysisDTO.Response.fromEntity(analysisRequest);
    }
}