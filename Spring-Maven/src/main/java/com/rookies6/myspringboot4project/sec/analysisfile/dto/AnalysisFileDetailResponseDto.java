package com.rookies6.myspringboot4project.sec.analysisfile.dto;

import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile;
import lombok.Getter;

import java.util.List;

@Getter
public class AnalysisFileDetailResponseDto {

    private final Long fileId;
    private final String fileName;
    private final String relativePath;
    private final String language;
    private final int lineCount;
    private final String content;
    private final List<FindingMarkerDto> findingMarkers;

    public AnalysisFileDetailResponseDto(AnalysisFile file) {
        this.fileId = file.getId();
        this.fileName = file.getFileName();
        this.relativePath = file.getRelativePath();
        this.language = file.getLanguage();

        this.lineCount = file.getLineCount() != null
                ? file.getLineCount()
                : 0;

        this.content = file.getContent();

        this.findingMarkers = file.getFindings() != null
                ? file.getFindings().stream()
                    .map(FindingMarkerDto::new)
                    .toList()
                : List.of();
    }
}
