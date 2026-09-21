package com.rookies6.myspringboot4project.sec.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Analysis {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String originalCode;
    
    @Column(nullable = false, length = 50)
    private String language;
    
    private int totalCount;
    private int highCount;
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vulnerability> vulnerabilities = new ArrayList<>();

    @Builder
    public Analysis(String originalCode, String language) {
        this.originalCode = originalCode;
        this.language = language;
    }

    public void updateResults(int totalCount, int highCount, List<Vulnerability> vulnerabilities) {
        this.totalCount = totalCount;
        this.highCount = highCount;
        this.vulnerabilities.clear();
        if (vulnerabilities != null) {
            vulnerabilities.forEach(v -> {
                v.setAnalysis(this);
                this.vulnerabilities.add(v);
            });
        }
    }
}