// 1. 패키지 경로 변경 (analysis -> analysisfile)
package com.rookies6.myspringboot4project.sec.analysisfile.dto; 

// 2. 다른 패키지에 있는 AnalysisDTO 임포트
import com.rookies6.myspringboot4project.sec.analysis.dto.AnalysisDTO; 
// 3. 엔티티 패키지 경로 변경 반영
import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile; 
import lombok.Getter;

import java.util.List;

public class AnalysisFileDTO {

    /**
     * 파일 목록 조회용 응답 (파일 내용 제외, 용량 최적화)
     */
    @Getter
    public static class Response {
        private final Long fileId;
        private final String fileName;
        private final String relativePath;
        private final String language;
        private final int lineCount;
        private final int findingCount;

        public Response(AnalysisFile file) {
            this.fileId = file.getId();
            this.fileName = file.getFileName();
            this.relativePath = file.getRelativePath();
            this.language = file.getLanguage();
            this.lineCount = file.getLineCount();
            this.findingCount = file.getFindings().size(); // 해당 파일의 취약점 개수
        }
    }

    /**
     * 파일 상세 조회용 응답 (소스 코드 내용 및 취약점 상세 정보 포함)
     */
    @Getter
    public static class DetailResponse {
        private final Long fileId;
        private final String fileName;
        private final String relativePath;
        private final String content;
        private final List<AnalysisDTO.VulnerabilityDto> vulnerabilities;

        public DetailResponse(AnalysisFile file) {
            this.fileId = file.getId();
            this.fileName = file.getFileName();
            this.relativePath = file.getRelativePath();
            this.content = file.getContent();
            // 해당 파일에 속한 취약점만 매핑
            this.vulnerabilities = file.getFindings().stream()
                    .map(AnalysisDTO.VulnerabilityDto::new)
                    .toList();
        }
    }
}