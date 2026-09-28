package com.rookies6.myspringboot4project.sec.project.entity;

import com.rookies6.myspringboot4project.common.entity.BaseEntity;
import com.rookies6.myspringboot4project.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "projects")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project extends BaseEntity { 
    // BaseEntity에 createdAt, updatedAt 필드가 있는지 반드시 확인하세요!
    // 없다면 아래와 같이 직접 추가하거나 BaseEntity를 수정해야 합니다.
    // @CreatedDate
    // @Column(updatable = false)
    // private LocalDateTime createdAt;
    // @LastModifiedDate
    // private LocalDateTime updatedAt;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // 프로젝트 이름

    @Column(length = 20)
    private String language; // 프로그래밍 언어

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Project(String name, String language, User user) {
        this.name = name;
        this.language = language;
        this.user = user;
    }

    // 이름 변경 메서드
    public void updateName(String name) {
        this.name = name;
    }

    // 프로젝트 전체 정보 수정 메서드
    public void update(String name, String language) {
        this.name = name;
        this.language = language;
    }
}