package com.rookies6.myspringboot4project.sec.codefile.dto;

import com.rookies6.myspringboot4project.sec.codefile.entity.CodeFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class CodeFileDTO {

    @Getter
    @NoArgsConstructor
    public static class Request {
        @NotNull(message = "프로젝트 ID는 필수입니다.")
        private Long projectId;

        @NotBlank(message = "파일 이름은 필수입니다.")
        private String fileName;

        @NotBlank(message = "언어 설정은 필수입니다.")
        private String language;

        @NotBlank(message = "소스 코드는 필수입니다.")
        private String sourceCode;
    }

    @Getter
    public static class Response {
        private final Long id;
        private final Long projectId;
        private final String fileName;
        private final String language;
        private final String sourceCode;
        private final LocalDateTime createdAt;

        public Response(CodeFile codeFile) {
            this.id = codeFile.getId();
            this.projectId = codeFile.getProject().getId();
            this.fileName = codeFile.getFileName();
            this.language = codeFile.getLanguage();
            this.sourceCode = codeFile.getSourceCode();
            this.createdAt = codeFile.getCreatedAt();
        }

        public static Response fromEntity(CodeFile codeFile) {
            return new Response(codeFile);
        }
    }
}