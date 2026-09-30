package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.exception.BusinessException;
import com.rookies6.myspringboot4project.exception.ErrorCode;
import com.rookies6.myspringboot4project.sec.analysis.dto.AnalysisRequestDto;
import com.rookies6.myspringboot4project.sec.analysis.dto.AnalysisResponseDto;
import com.rookies6.myspringboot4project.sec.analysis.dto.AnalysisStatusResponseDto;
import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile;
import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisStatus;
import com.rookies6.myspringboot4project.sec.analysis.repository.AnalysisRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisService {

    private final AnalysisRequestRepository analysisRequestRepository;

    /**
     * 1. 전체 분석 히스토리 목록 조회
     */
    public List<AnalysisResponseDto> getAllAnalyses() {
        return analysisRequestRepository.findAll()
                .stream()
                .map(AnalysisResponseDto::fromEntity)
                .toList();
    }

    /**
     * 2. ID로 분석 결과 상세 조회
     */
    public AnalysisResponseDto getAnalysisById(Long id) {
        AnalysisRequest request = analysisRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND, "AnalysisRequest", "id", id
                ));
        return AnalysisResponseDto.fromEntity(request);
    }

    /**
     * 3. 비동기 스캔 진행 상태 조회
     */
    public AnalysisStatusResponseDto getAnalysisStatus(Long id) {
        AnalysisRequest request = analysisRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND, "AnalysisRequest", "id", id
                ));
        return AnalysisStatusResponseDto.fromEntity(request);
    }

    /**
     * 4. 새 분석 요청 생성 및 파일 엔티티 매핑
     */
    @Transactional
    public AnalysisResponseDto createAnalysis(AnalysisRequestDto requestDto) {
        // AnalysisRequest 엔티티 생성 (초기 상태: PENDING)
        AnalysisRequest request = AnalysisRequest.builder()
                .title(requestDto.getTitle())
                .language(requestDto.getLanguage())
                .status(AnalysisStatus.PENDING)
                .build();

        // 첨부 파일 목록 매핑 및 연관관계 설정
        if (requestDto.getFiles() != null && !requestDto.getFiles().isEmpty()) {
            List<AnalysisFile> files = requestDto.getFiles().stream()
                    .map(fileDto -> {
                        String path = fileDto.getFilePath();
                        String fileName = path.contains("/") 
                                ? path.substring(path.lastIndexOf("/") + 1) 
                                : path;
                        int lineCount = fileDto.getContent() != null 
                                ? fileDto.getContent().split("\n").length 
                                : 0;

                        return AnalysisFile.builder()
                                .fileName(fileName)
                                .relativePath(path)
                                .language(requestDto.getLanguage())
                                .lineCount(lineCount)
                                .content(fileDto.getContent())
                                .analysisRequest(request)
                                .build();
                    })
                    .toList();

            request.getAnalysisFiles().addAll(files);
        }

        AnalysisRequest savedRequest = analysisRequestRepository.save(request);

        return AnalysisResponseDto.fromEntity(savedRequest);
    }

    /**
     * 5. 분석 히스토리 삭제
     */
    @Transactional
    public void deleteAnalysis(Long id) {
        if (!analysisRequestRepository.existsById(id)) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND, "AnalysisRequest", "id", id
            );
        }
        analysisRequestRepository.deleteById(id);
    }


    // 유틸리티 메서드: 경로에서 파일명만 추출
    private String extractFileName(String filePath) {
        if (filePath == null) return "unknown";
        int lastSlash = filePath.lastIndexOf('/');
        int lastBackslash = filePath.lastIndexOf('\\');
        int maxIndex = Math.max(lastSlash, lastBackslash);
        return maxIndex >= 0 ? filePath.substring(maxIndex + 1) : filePath;
    }
}