package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.exception.BusinessException;
import com.rookies6.myspringboot4project.exception.ErrorCode;
import com.rookies6.myspringboot4project.sec.analysis.dto.FindingStatusDTO;
import com.rookies6.myspringboot4project.sec.analysis.entity.FindingVulnerability;
import com.rookies6.myspringboot4project.sec.analysis.repository.FindingVulnerabilityRepository;
import com.rookies6.myspringboot4project.sec.common.enums.FindingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindingStatusService {

    private final FindingVulnerabilityRepository findingVulnerabilityRepository;

    @Transactional
    public FindingStatusDTO.Response changeStatus(Long findingId, FindingStatusDTO.Request request) {
        // 잘못된 상태값은 400
        parseStatus(request.getStatus());

        // 존재하지 않는 ID는 404
        FindingVulnerability finding = findingVulnerabilityRepository.findById(findingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FINDING_NOT_FOUND, findingId));

        // 이미 RESOLVED 상태면 409
        if (finding.getStatus() == FindingStatus.RESOLVED) {
            throw new BusinessException(ErrorCode.FINDING_ALREADY_RESOLVED, findingId);
        }

        // 해당 ID가 존재하고, 해결상태가 RESOLVED가 아니면 RESOLVED로 변경
        finding.resolve();

        return FindingStatusDTO.Response.from(finding);
    }

    private FindingStatus parseStatus(String status) {
        try {
            return FindingStatus.valueOf(status);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ErrorCode.INVALID_FINDING_STATUS, status);
        }
    }
}
