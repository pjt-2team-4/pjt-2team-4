package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.exception.BusinessException;
import com.rookies6.myspringboot4project.exception.ErrorCode;
import com.rookies6.myspringboot4project.sec.analysis.dto.FindingDTO;
import com.rookies6.myspringboot4project.sec.analysis.entity.FindingVulnerability;
import com.rookies6.myspringboot4project.sec.analysis.repository.AnalysisRequestRepository;
import com.rookies6.myspringboot4project.sec.analysis.repository.FindingVulnerabilityRepository;
import com.rookies6.myspringboot4project.sec.common.enums.FindingStatus;
import com.rookies6.myspringboot4project.sec.common.enums.Severity;
import com.rookies6.myspringboot4project.sec.common.enums.VulnerabilityType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindingService {

    private final FindingVulnerabilityRepository findingRepository;
    private final AnalysisRequestRepository requestRepository;

    // TODO: 예빈님 인증 머지 후 소유권 검사(본인 분석만 조회) 추가

    /** 12번 — 파일 경로·라인 순으로 페이지 조회 */
    public FindingDTO.ListResponse getFindings(Long analysisId, VulnerabilityType type,
                                               String ruleId, FindingStatus status,
                                               List<Severity> severities, Long fileId,
                                               int page, int size) {
        if (page < 0 || size < 1 || size > 50) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "page는 0 이상, size는 1~50이어야 합니다.");
        }
        if (!requestRepository.existsById(analysisId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "분석 요청을 찾을 수 없습니다: " + analysisId);
        }
        List<Severity> selectedSeverities = severities == null || severities.isEmpty()
                ? List.of(Severity.values()) : List.copyOf(severities);
        Page<FindingVulnerability> result = findingRepository.findAllByAnalysisId(
                analysisId, type, ruleId, status, fileId, selectedSeverities,
                PageRequest.of(page, size));
        List<FindingDTO.ListItem> content = result.getContent().stream()
                .map(FindingDTO.ListItem::from)
                .toList();
        return new FindingDTO.ListResponse(content, new FindingDTO.PageInfo(
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages()));
    }

    /** 13번 */
    public FindingDTO.Detail getFinding(Long findingId) {
        FindingVulnerability finding = findingRepository.findDetailById(findingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FINDING_NOT_FOUND));
        return FindingDTO.Detail.from(finding);
    }
}
