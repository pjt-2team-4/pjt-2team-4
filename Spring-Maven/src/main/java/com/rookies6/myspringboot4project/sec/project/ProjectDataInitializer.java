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
                Project.builder().name("스프링 시큐어 뱅킹 시스템").language("java").build(),
                Project.builder().name("이커머스 결제 API 서버").language("java").build(),
                Project.builder().name("사용자 인증 마이크로서비스").language("java").build(),
                Project.builder().name("온라인 강의 플랫폼 백엔드").language("java").build(),
                Project.builder().name("사내 게시판 및 파일 관리").language("java").build()
            );
            projectRepository.saveAll(sampleProjects);
        }
    }
}