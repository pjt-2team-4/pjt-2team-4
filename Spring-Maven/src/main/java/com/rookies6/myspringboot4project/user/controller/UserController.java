package com.rookies6.myspringboot4project.user.controller;

import com.rookies6.myspringboot4project.user.dto.UserDTO;
import com.rookies6.myspringboot4project.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users") // 📌 /api/v1/users 로 버전 명시 및 복수형 정리
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 전체 유저 목록 조회 (Admin 전용)
     * GET /api/v1/users
     */
    @GetMapping
    public ResponseEntity<List<UserDTO.Response>> getAllUsers() {
        List<UserDTO.Response> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * ID로 유저 조회
     * GET /api/v1/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO.Response> getUserById(@PathVariable Long id) {
        UserDTO.Response user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * 이메일로 유저 조회
     * GET /api/v1/users/email/{email}
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO.Response> getUserByEmail(@PathVariable String email) {
        UserDTO.Response user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    /**
     * 회원가입
     * POST /api/v1/users/signup (또는 POST /api/v1/users)
     */
    @PostMapping
    public ResponseEntity<UserDTO.Response> createUser(@Valid @RequestBody UserDTO.Request request) {
        UserDTO.Response createdUser = userService.createUser(request);
        return ResponseEntity.ok(createdUser);
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDTO.Response> signUpUser(@Valid @RequestBody UserDTO.Request request) {
        UserDTO.Response createdUser = userService.createUser(request);
        return ResponseEntity.ok(createdUser);
    }

    /**
     * 로그인
     * POST /api/v1/users/login
     */
    @PostMapping("/login")
    public ResponseEntity<UserDTO.Response> loginUser(@RequestBody UserDTO.LoginRequest loginRequest) {
        UserDTO.Response loggedInUser = userService.login(loginRequest);
        return ResponseEntity.ok(loggedInUser);
    }

    /**
     * 유저 정보 수정
     * PUT /api/v1/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO.Response> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO.Request request) {

        UserDTO.Response updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * 유저 삭제
     * DELETE /api/v1/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}