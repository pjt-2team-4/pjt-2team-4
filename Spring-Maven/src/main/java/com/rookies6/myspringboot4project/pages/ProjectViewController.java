// src/main/java/com/rookies6/myspringboot4project/sec/controller/ProjectViewController.java
package com.rookies6.myspringboot4project.pages;

import com.rookies6.myspringboot4project.sec.project.entity.Project;
import com.rookies6.myspringboot4project.sec.project.repository.ProjectRepository;
import com.rookies6.myspringboot4project.user.entity.User;
import com.rookies6.myspringboot4project.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectViewController {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // 1. 프로젝트 목록 관리 화면 (Read)
    @GetMapping
    public String listProjects() {
        // GlobalControllerAdvice를 통해 'projects' 데이터가 이미 들어있으므로 바로 화면 반환
        return "project/list";
    }

    // 2. 프로젝트 생성 화면 (Create - Form)
    @GetMapping("/new")
    public String createProjectForm() {
        return "project/create"; 
    }

    // 3. 프로젝트 생성 처리 (Create - Action)
    @PostMapping("/new")
    public String createProject(@RequestParam String name) {
        User user = userRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("등록된 회원(User)이 없습니다."));

        Project project = Project.builder()
                .user(user)
                .name(name)
                .build();
        
        projectRepository.save(project);
        return "redirect:/projects"; // 생성 후 목록으로 이동
    }

    // 4. 프로젝트 수정 화면 (Update - Form)
    @GetMapping("/{id}/edit")
    public String editProjectForm(@PathVariable Long id, Model model) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));
        model.addAttribute("project", project);
        return "project/edit";
    }

    // 5. 프로젝트 수정 처리 (Update - Action)
    @PostMapping("/{id}/edit")
    public String updateProject(@PathVariable Long id, @RequestParam String name) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));
        
        project.updateName(name); // 이름 변경
        projectRepository.save(project); // 영속성 반영
        return "redirect:/projects";
    }

    // 6. 프로젝트 삭제 처리 (Delete - Action)
    @PostMapping("/{id}/delete")
    public String deleteProject(@PathVariable Long id) {
        // 💡 엔티티에 설정된 CascadeType.ALL 덕분에 
        // 프로젝트 삭제 시 연관된 AnalysisRequest와 VulnerabilityFinding도 함께(자동) 삭제됩니다.
        projectRepository.deleteById(id);
        return "redirect:/projects";
    }
}