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
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserViewController {

    private final UserService userService;

    // 1. 메인 허브 화면 (root index.html)
    @GetMapping("/")
    public String index() {
        return "index"; // templates/index.html
    }

    // 2. 회원 목록 화면
    @GetMapping("/list-users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "user/list-users"; // templates/user/list-users.html
    }

    // 3. 회원 등록 화면
    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("userForm", new UserDTO.Request());
        return "user/signup"; // templates/user/signup.html
    }

    // 4. 회원 등록 처리
    @PostMapping("/signup")
    public String addUser(
            @Valid @ModelAttribute("userForm") UserDTO.Request request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "user/signup";
        }

        userService.createUser(request);
        return "redirect:/user/list-users";
    }

    // 5. 회원 수정 화면
    @GetMapping("/edit-user/{id}")
    public String editUser(Model model, @PathVariable Long id) {
        model.addAttribute("userForm", userService.getUserById(id));
        return "user/edit-user"; // templates/user/edit-user.html
    }

    // 6. 회원 수정 처리
    @PostMapping("/edit-user/{id}")
    public String updateUser(
            @PathVariable Long id,
            @ModelAttribute("userForm") UserDTO.Request request) {
        
        userService.updateUser(id, request);
        return "redirect:/user/list-users";
    }
}