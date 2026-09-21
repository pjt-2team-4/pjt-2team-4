package com.rookies6.myspringboot4project.controller;

import com.rookies6.myspringboot4project.dto.StudentDTO;
import com.rookies6.myspringboot4project.service.StudentService;
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

    // 학생 목록
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("students", studentService.getAllStudents());
        return "index";
    }

    // 학생 등록 화면
    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("studentForm", new StudentDTO.Request());
        return "add-user";
    }

    // 학생 등록
    @PostMapping("/adduser")
    public String addUser(
            @Valid @ModelAttribute("studentForm") StudentDTO.Request request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "add-user";
        }

        studentService.createStudent(request);

        return "redirect:/";
    }
}
