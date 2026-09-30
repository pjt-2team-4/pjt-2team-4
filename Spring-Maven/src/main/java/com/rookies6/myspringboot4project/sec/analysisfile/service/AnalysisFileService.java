package com.rookies6.myspringboot4project.sec.analysisfile.service;

import com.rookies6.myspringboot4project.exception.BusinessException;
import com.rookies6.myspringboot4project.exception.ErrorCode;
import com.rookies6.myspringboot4project.sec.analysisfile.dto.AnalysisFileDetailResponseDto;
import com.rookies6.myspringboot4project.sec.analysisfile.dto.AnalysisFileListResponseDto;
import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile;
import com.rookies6.myspringboot4project.sec.analysisfile.repository.AnalysisFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisFileService {

    private final AnalysisFileRepository analysisFileRepository;

    /**
     * 1. 특정 분석 요청(AnalysisRequest) ID에 포함된 파일 목록 조회 (용량 최적화용 - content 제외)
     */
    public List<AnalysisFileListResponseDto> getFilesByAnalysisRequestId(Long analysisRequestId) {
        return analysisFileRepository.findByAnalysisRequestId(analysisRequestId)
                .stream()
                .map(AnalysisFileListResponseDto::new)
                .toList();
    }

    /**
     * 2. 단일 분석 파일 상세 조회 (소스 코드 원문 및 해당 파일의 취약점 목록 포함)
     */
    public AnalysisFileDetailResponseDto getFileById(Long fileId) {
        AnalysisFile file = analysisFileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND, "AnalysisFile", "id", fileId
                ));
        return new AnalysisFileDetailResponseDto(file);
    }
}