package com.rookies6.myspringboot4project.sec.analysis.dto;

import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisStatusResponseDto {

    private Long analysisId;
    private String status;
    private String errorMessage;

    public static AnalysisStatusResponseDto fromEntity(AnalysisRequest request) {
        return AnalysisStatusResponseDto.builder()
                .analysisId(request.getId())
                .status(request.getStatus() != null ? request.getStatus().name() : null)
                .errorMessage(request.getErrorMessage())
                .build();
    }
}