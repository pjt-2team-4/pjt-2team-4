package com.rookies6.myspringboot4project.sec.projectfile.entity;

import com.rookies6.myspringboot4project.common.entity.BaseEntity;
import com.rookies6.myspringboot4project.sec.project.entity.Project;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "project_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectFile extends BaseEntity { // BaseEntity 상속으로 createdAt, updatedAt 자동 관리

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 255)
    private String filePath; // 파일 경로 추가

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, length = 20) 
    private String language;

    @Lob
    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content; // sourceCode -> content 로 이름 변경 및 타입 지정

    @Column(nullable = false)
    private Integer fileSizeBytes; // 파일 사이즈 추가

    @Builder
    public ProjectFile(Project project, String filePath, String fileName, String language, String content, Integer fileSizeBytes) {
        this.project = project;
        this.filePath = filePath;
        this.fileName = fileName;
        this.language = language;
        this.content = content;
        this.fileSizeBytes = fileSizeBytes;
    }

    // 파일 내용 및 정보 수정 메서드
    public void updateContent(String filePath, String fileName, String content, String language, Integer fileSizeBytes) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.content = content;
        this.language = language;
        this.fileSizeBytes = fileSizeBytes;
    }
}