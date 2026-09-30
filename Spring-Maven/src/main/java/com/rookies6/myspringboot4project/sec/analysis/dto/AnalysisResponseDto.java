package com.rookies6.myspringboot4project.sec.analysis.dto;

import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
// 📌 올바른 FindingVulnerability import 경로
import com.rookies6.myspringboot4project.sec.common.enums.FindingVulnerability;
import com.rookies6.myspringboot4project.sec.common.enums.Severity;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Getter
public class AnalysisResponseDto {

    private final Long analysisId;
    private final String title;
    private final String language;
    private final String status;

    private final int totalCount;
    private final long criticalCount;
    private final long highCount;
    private final long mediumCount;
    private final long lowCount;

    private final List<AnalysisFileDto> files;
    private final List<VulnerabilityDto> vulnerabilities;
    private final LocalDateTime createdAt;

    public AnalysisResponseDto(AnalysisRequest request) {
        this.analysisId = request.getId();
        this.title = request.getTitle();
        this.language = request.getLanguage();
        this.status = request.getStatus() != null ? request.getStatus().name() : null;

        this.files = request.getAnalysisFiles() != null ? request.getAnalysisFiles().stream()
                .map(file -> new AnalysisFileDto(
                        file.getId(),
                        file.getRelativePath() != null ? file.getRelativePath() : file.getFileName(),
                        file.getContent()
                ))
                .toList() : List.of();

        List<FindingVulnerability> allFindings = request.getAnalysisFiles() != null ? request.getAnalysisFiles().stream()
                .flatMap(file -> file.getFindings() != null ? file.getFindings().stream() : Stream.empty())
                .toList() : List.of();

        this.totalCount = allFindings.size();

        this.criticalCount = allFindings.stream().filter(f -> f.getSeverity() == Severity.CRITICAL).count();
        this.highCount = allFindings.stream().filter(f -> f.getSeverity() == Severity.HIGH).count();
        this.mediumCount = allFindings.stream().filter(f -> f.getSeverity() == Severity.MEDIUM).count();
        this.lowCount = allFindings.stream().filter(f -> f.getSeverity() == Severity.LOW).count();

        this.vulnerabilities = allFindings.stream()
                .map(VulnerabilityDto::new)
                .toList();

        this.createdAt = request.getCreatedAt();
    }

    public static AnalysisResponseDto fromEntity(AnalysisRequest request) {
        return new AnalysisResponseDto(request);
    }
}