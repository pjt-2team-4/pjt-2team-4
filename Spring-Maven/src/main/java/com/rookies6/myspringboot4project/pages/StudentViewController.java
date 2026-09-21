package com.rookies6.myspringboot4project.pages;

import com.rookies6.myspringboot4project.user.dto.StudentDTO;
import com.rookies6.myspringboot4project.user.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class StudentViewController {

    private final StudentService studentService;

    // 1. 메인 허브 화면 (단순 페이지 연결용)
    @GetMapping("/")
    public String index() {
        return "index"; // templates/index.html 반환
    }

    // 2. 학생 목록 화면 (목록 데이터 전달)
    @GetMapping("/list-users")
    public String listUsers(Model model) {
        model.addAttribute("students", studentService.getAllStudents());
        return "list-users"; // templates/list-users.html 반환
    }

    // 3. 학생 등록 화면 (GET)
    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("studentForm", new StudentDTO.Request());
        return "signup"; // templates/signup.html 반환
    }

    // 4. 학생 등록 처리 (POST)
    @PostMapping("/signup")
    public String addUser(
            @Valid @ModelAttribute("studentForm") StudentDTO.Request request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "signup";
        }

        studentService.createStudent(request);
        return "redirect:/list-users"; // 등록 성공 후 메인이 아닌 '학생 목록'으로 이동
    }

    // 5. 학생 수정 화면 (GET)
    @GetMapping("/edit-user/{id}")
    public String editUser(Model model, @PathVariable Long id) {
        model.addAttribute("studentForm", studentService.getStudentById(id));
        return "edit-user"; // templates/edit-user.html 반환
    }

    // 6. 학생 수정 처리 (POST)
    @PostMapping("/edit-user/{id}")
    public String updateUser(
            @PathVariable Long id,
            @ModelAttribute("studentForm") StudentDTO.Request request) {
        
        studentService.updateStudent(id, request);
        return "redirect:/list-users"; // 수정 성공 후 메인이 아닌 '학생 목록'으로 이동
    }
    
    // 7. API 가이드 화면 (GET)
    @GetMapping("/api-guide")
    public String apiGuide() {
        return "api-guide"; // templates/api-guide.html 반환
    }

    // 8. 코드 보안 분석 화면 (GET)
    @GetMapping("/analysis")
    public String analysis() {
        return "analysis"; // templates/analysis.html 반환
    }
}