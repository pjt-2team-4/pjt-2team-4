
package com.rookies6.myspringboot4project.sec.project;

import com.rookies6.myspringboot4project.sec.project.entity.Project;
import com.rookies6.myspringboot4project.sec.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProjectDataInitializer implements CommandLineRunner {

    private final ProjectRepository projectRepository;

    @Override
    public void run(String... args) {
        if (projectRepository.count() == 0) {
            List<Project> sampleProjects = List.of(
                Project.builder().name("스프링 시큐어 뱅킹 시스템").description("금융 데이터 취약점 점검 프로젝트").language("java").build(),
                Project.builder().name("이커머스 결제 API 서버").description("온라인 쇼핑몰 결제 모듈 보안 검사").language("java").build(),
                Project.builder().name("사용자 인증 마이크로서비스").description("OAuth2 및 JWT 기반 인증 서버").language("java").build(),
                Project.builder().name("온라인 강의 플랫폼 백엔드").description("수강 신청 및 강의 관리 시스템").language("java").build(),
                Project.builder().name("사내 게시판 및 파일 관리").description("인트라넷 문서 공유 시스템").language("java").build()
            );
            projectRepository.saveAll(sampleProjects);
        }
    }
}