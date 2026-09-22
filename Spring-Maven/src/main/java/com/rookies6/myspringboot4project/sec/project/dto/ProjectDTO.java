package com.rookies6.myspringboot4project.sec.project.dto;

import com.rookies6.myspringboot4project.sec.project.entity.Project;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ProjectDTO {

    @Getter
    @NoArgsConstructor
    public static class Request {
        @NotBlank(message = "프로젝트 이름은 필수입니다.")
        private String name;

        private String description;

        @NotBlank(message = "사용 언어는 필수입니다.")
        private String language;

        public Project toEntity() {
            return Project.builder()
                    .name(name)
                    .description(description)
                    .language(language)
                    .build();
        }
    }

    @Getter
    public static class Response {
        private final Long id;
        private final String name;
        private final String description;
        private final String language;
        private final LocalDateTime createdAt;

        public Response(Project project) {
            this.id = project.getId();
            this.name = project.getName();
            this.description = project.getDescription();
            this.language = project.getLanguage();
            this.createdAt = project.getCreatedAt();
        }

        public static Response fromEntity(Project project) {
            return new Response(project);
        }
    }
}