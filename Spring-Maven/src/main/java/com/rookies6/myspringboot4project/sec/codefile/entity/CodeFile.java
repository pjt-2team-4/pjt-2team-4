package com.rookies6.myspringboot4project.sec.codefile.entity;

import com.rookies6.myspringboot4project.sec.project.entity.Project;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "code_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CodeFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, length = 50)
    private String language;

    @Lob
    @Column(nullable = false)
    private String sourceCode;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public CodeFile(Project project, String fileName, String language, String sourceCode) {
        this.project = project;
        this.fileName = fileName;
        this.language = language;
        this.sourceCode = sourceCode;
    }

    public void updateContent(String fileName, String sourceCode, String language) {
        this.fileName = fileName;
        this.sourceCode = sourceCode;
        this.language = language;
    }
}