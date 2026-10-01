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
     * 특정 분석에 포함된 파일 목록 조회
     *
     * GET /api/v1/analyses/{analysisId}/files
     */
    public List<AnalysisFileListResponseDto> getFilesByAnalysisRequestId(
            Long analysisId
    ) {

        return analysisFileRepository
                .findByAnalysisRequestId(analysisId)
                .stream()
                .map(AnalysisFileListResponseDto::new)
                .toList();
    }

    /**
     * 특정 분석에 포함된 특정 파일 상세 조회
     *
     * GET /api/v1/analyses/{analysisId}/files/{fileId}
     *
     * analysisId와 fileId가 실제로 연결되어 있는지도 확인한다.
     */
    public AnalysisFileDetailResponseDto getFileById(
            Long analysisId,
            Long fileId
    ) {

        AnalysisFile file = analysisFileRepository
                .findById(fileId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "AnalysisFile",
                        "id",
                        fileId
                ));

        /*
         * fileId만 존재하고 analysisId가 다른 경우를 방지한다.
         */
        if (file.getAnalysisRequest() == null
                || !file.getAnalysisRequest().getId().equals(analysisId)) {

            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "AnalysisFile",
                    "id",
                    fileId
            );
        }

        return new AnalysisFileDetailResponseDto(file);
    }
}
