package com.rookies6.myspringboot4project.sec.analysisfile.dto;

import com.rookies6.myspringboot4project.sec.analysis.dto.VulnerabilityDto;
import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile;
import lombok.Getter;

import java.util.List;

@Getter
public class AnalysisFileDetailResponseDto {

    private final Long fileId;
    private final String fileName;
    private final String relativePath;
    private final String content;
    private final List<VulnerabilityDto> vulnerabilities;

    public AnalysisFileDetailResponseDto(AnalysisFile file) {
        this.fileId = file.getId();
        this.fileName = file.getFileName();
        this.relativePath = file.getRelativePath();
        this.content = file.getContent();
        this.vulnerabilities = file.getFindings() != null ? file.getFindings().stream()
                .map(VulnerabilityDto::new)
                .toList() : List.of();
    }
}