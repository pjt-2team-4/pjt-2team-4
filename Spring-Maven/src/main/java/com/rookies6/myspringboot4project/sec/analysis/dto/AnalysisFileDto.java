package com.rookies6.myspringboot4project.sec.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnalysisFileDto {

    private final Long id;
    private final String filePath;
    private final String content;
}