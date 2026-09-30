package com.rookies6.myspringboot4project.sec.analysisfile.dto;

import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile;
import lombok.Getter;

@Getter
public class AnalysisFileListResponseDto {

    private final Long fileId;
    private final String fileName;
    private final String relativePath;
    private final String language;
    private final int lineCount;
    private final int findingCount;

    public AnalysisFileListResponseDto(AnalysisFile file) {
        this.fileId = file.getId();
        this.fileName = file.getFileName();
        this.relativePath = file.getRelativePath();
        this.language = file.getLanguage();
        this.lineCount = file.getLineCount();
        this.findingCount = file.getFindings() != null ? file.getFindings().size() : 0;
    }
}