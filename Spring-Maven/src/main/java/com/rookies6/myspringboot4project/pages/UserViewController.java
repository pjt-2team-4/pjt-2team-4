package com.rookies6.myspringboot4project.pages;

import com.rookies6.myspringboot4project.user.dto.UserDTO;
import com.rookies6.myspringboot4project.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class UserViewController {

    private final UserService userService;

    // 1. 메인 허브 화면
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // 2. 회원 목록 화면
    @GetMapping("/list-users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "list-users";
    }

    // 3. 회원 등록 화면
    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("userForm", new UserDTO.Request());
        return "signup";
    }

    // 4. 회원 등록 처리
    @PostMapping("/signup")
    public String addUser(
            @Valid @ModelAttribute("userForm") UserDTO.Request request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "signup";
        }

        userService.createUser(request);
        return "redirect:/list-users";
    }

    // 5. 회원 수정 화면
    @GetMapping("/edit-user/{id}")
    public String editUser(Model model, @PathVariable Long id) {
        model.addAttribute("userForm", userService.getUserById(id));
        return "edit-user";
    }

    // 6. 회원 수정 처리
    @PostMapping("/edit-user/{id}")
    public String updateUser(
            @PathVariable Long id,
            @ModelAttribute("userForm") UserDTO.Request request) {
        
        userService.updateUser(id, request);
        return "redirect:/list-users";
    }
    
    // 7. API 가이드 화면
    @GetMapping("/api-guide")
    public String apiGuide() {
        return "api-guide";
    }

    // 8. 코드 보안 분석 화면
    @GetMapping("/analysis")
    public String analysis() {
        return "analysis";
    }
}