package com.rookies6.myspringboot4project.sec.analysisfile.dto;

import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile;
import com.rookies6.myspringboot4project.sec.common.enums.Severity;
import lombok.Getter;

@Getter
public class AnalysisFileListResponseDto {

    private final Long fileId;
    private final String fileName;
    private final String relativePath;
    private final String language;
    private final long fileSizeBytes;
    private final int lineCount;
    private final int findingCount;
    private final String highestSeverity;

    public AnalysisFileListResponseDto(AnalysisFile file) {
        this.fileId = file.getId();
        this.fileName = file.getFileName();
        this.relativePath = file.getRelativePath();
        this.language = file.getLanguage();

        this.fileSizeBytes = file.getContent() != null
                ? file.getContent().getBytes(java.nio.charset.StandardCharsets.UTF_8).length
                : 0;

        this.lineCount = file.getLineCount() != null
                ? file.getLineCount()
                : 0;

        this.findingCount = file.getFindings() != null
                ? file.getFindings().size()
                : 0;

        this.highestSeverity = calculateHighestSeverity(file);
    }

    private String calculateHighestSeverity(AnalysisFile file) {
        if (file.getFindings() == null || file.getFindings().isEmpty()) {
            return null;
        }

        if (file.getFindings().stream()
                .anyMatch(f -> f.getSeverity() == Severity.CRITICAL)) {
            return Severity.CRITICAL.name();
        }

        if (file.getFindings().stream()
                .anyMatch(f -> f.getSeverity() == Severity.HIGH)) {
            return Severity.HIGH.name();
        }

        if (file.getFindings().stream()
                .anyMatch(f -> f.getSeverity() == Severity.MEDIUM)) {
            return Severity.MEDIUM.name();
        }

        if (file.getFindings().stream()
                .anyMatch(f -> f.getSeverity() == Severity.LOW)) {
            return Severity.LOW.name();
        }

        return null;
    }
}
