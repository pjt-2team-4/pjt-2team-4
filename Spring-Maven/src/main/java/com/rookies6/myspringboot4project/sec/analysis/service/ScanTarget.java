package com.rookies6.myspringboot4project.sec.analysis.service;

public record ScanTarget(
        Long fileId,
        String relativePath,
        String fileName,
        String content
) {
}