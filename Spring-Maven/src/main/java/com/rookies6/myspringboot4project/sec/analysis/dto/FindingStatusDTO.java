package com.rookies6.myspringboot4project.sec.analysis.dto;

import com.rookies6.myspringboot4project.sec.analysis.entity.FindingVulnerability;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class FindingStatusDTO {

    @Getter
    @Setter
    @Builder
    public static class Request {

        @NotBlank(message = "변경할 상태는 필수입니다.")
        private String status;   // OPEN / RESOLVED / IGNORED
    }

    @Getter
    @Builder
    public static class Response {
        private Long findingId;
        private String status;
        private LocalDateTime updatedAt;

        public static Response from(FindingVulnerability finding) {
            return Response.builder()
                    .findingId(finding.getId())
                    .status(finding.getStatus().name())
                    .updatedAt(finding.getUpdatedAt())
                    .build();
        }
    }
}
