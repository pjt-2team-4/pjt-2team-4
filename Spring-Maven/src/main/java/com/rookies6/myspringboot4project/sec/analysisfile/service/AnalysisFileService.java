package com.rookies6.myspringboot4project.sec.analysisfile.service;

import com.rookies6.myspringboot4project.sec.analysisfile.dto.AnalysisFileDTO;
import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile;
import com.rookies6.myspringboot4project.sec.analysisfile.repository.AnalysisFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisFileService {

    private final AnalysisFileRepository analysisFileRepository;

    /**
     * 분석 ID(프로젝트 ID)에 속한 전체 파일 목록 조회
     */
    public List<AnalysisFileDTO.Response> getFilesByProjectId(Long analysisId) {
        List<AnalysisFile> files = analysisFileRepository.findByAnalysisRequestId(analysisId);
        return files.stream()
                .map(AnalysisFileDTO.Response::new)
                .collect(Collectors.toList());
    }

    /**
     * 파일 상세 조회 (무결성 검증 포함)
     */
    public AnalysisFileDTO.DetailResponse getFileDetail(Long analysisId, Long fileId) {
        AnalysisFile file = analysisFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("해당 파일을 찾을 수 없습니다."));

        // IDOR 취약점 방어: 파일이 속한 분석 ID가 일치하는지 검증
        if (file.getAnalysisRequest() == null || !file.getAnalysisRequest().getId().equals(analysisId)) {
            throw new IllegalArgumentException("요청한 분석에 속하지 않는 파일입니다.");
        }

        return new AnalysisFileDTO.DetailResponse(file);
    }
}