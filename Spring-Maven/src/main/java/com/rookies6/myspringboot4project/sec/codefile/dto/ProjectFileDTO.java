package com.rookies6.myspringboot4project.sec.projectfile.dto;

import com.rookies6.myspringboot4project.sec.projectfile.entity.ProjectFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ProjectFileDTO {

    @Getter
    @NoArgsConstructor
    public static class Request {
        @NotNull(message = "프로젝트 ID는 필수입니다.")
        private Long projectId;

        @NotBlank(message = "파일 경로는 필수입니다.")
        private String filePath;

        @NotBlank(message = "파일 이름은 필수입니다.")
        private String fileName;

        @NotBlank(message = "언어 설정은 필수입니다.")
        private String language;

        @NotBlank(message = "파일 내용은 필수입니다.")
        private String content;

        @NotNull(message = "파일 사이즈는 필수입니다.")
        private Integer fileSizeBytes;
    }

    @Getter
    public static class Response {
        private final Long id;
        private final Long projectId;
        private final String filePath;
        private final String fileName;
        private final String language;
        private final String content;
        private final Integer fileSizeBytes;
        private final LocalDateTime createdAt;

        public Response(ProjectFile projectFile) {
            this.id = projectFile.getId();
            this.projectId = projectFile.getProject().getId();
            this.filePath = projectFile.getFilePath();
            this.fileName = projectFile.getFileName();
            this.language = projectFile.getLanguage();
            this.content = projectFile.getContent();
            this.fileSizeBytes = projectFile.getFileSizeBytes();
            this.createdAt = projectFile.getCreatedAt();
        }

        public static Response fromEntity(ProjectFile projectFile) {
            return new Response(projectFile);
        }
    }
}