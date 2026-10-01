package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.exception.BusinessException;
import com.rookies6.myspringboot4project.exception.ErrorCode;
import com.rookies6.myspringboot4project.sec.analysis.dto.FindingDTO;
import com.rookies6.myspringboot4project.sec.analysis.entity.FindingVulnerability;
import com.rookies6.myspringboot4project.sec.analysis.repository.AnalysisRequestRepository;
import com.rookies6.myspringboot4project.sec.analysis.repository.FindingVulnerabilityRepository;
import com.rookies6.myspringboot4project.sec.common.enums.FindingStatus;
import com.rookies6.myspringboot4project.sec.common.enums.VulnerabilityType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindingService {

    private final FindingVulnerabilityRepository findingRepository;
    private final AnalysisRequestRepository requestRepository;

    // TODO: 예빈님 인증 머지 후 소유권 검사(본인 분석만 조회) 추가

    /** 12번 — 심각도 높은 순, 같은 심각도는 파일 경로·라인 순 */
    public List<FindingDTO.ListItem> getFindings(Long analysisId, VulnerabilityType type,
                                                 String ruleId, FindingStatus status) {
        if (!requestRepository.existsById(analysisId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "분석 요청을 찾을 수 없습니다: " + analysisId);
        }
        return findingRepository.findAllByAnalysisId(analysisId, type, ruleId, status).stream()
                .sorted(Comparator.comparingInt((FindingVulnerability f) -> f.getSeverity().getWeight()).reversed())
                .map(FindingDTO.ListItem::from)
                .toList();
    }

    /** 13번 */
    public FindingDTO.Detail getFinding(Long findingId) {
        FindingVulnerability finding = findingRepository.findDetailById(findingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "취약점을 찾을 수 없습니다: " + findingId));
        return FindingDTO.Detail.from(finding);
    }
}