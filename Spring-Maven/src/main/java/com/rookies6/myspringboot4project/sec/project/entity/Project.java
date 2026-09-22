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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // 프로젝트 이름

    @Column(length = 500)
    private String description; // 프로젝트 설명 추가

    @Column(length = 50)
    private String language; // 프로그래밍 언어 추가

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Project(String name, String description, String language, User user) {
        this.name = name;
        this.description = description;
        this.language = language;
        this.user = user;
    }

    // 이름 변경 메서드
    public void updateName(String name) {
        this.name = name;
    }

    // 프로젝트 전체 정보 수정 메서드 추가 (Service에서 호출하는 update 대응)
    public void update(String name, String description, String language) {
        this.name = name;
        this.description = description;
        this.language = language;
    }
}