package com.rookies6.myspringboot4project.user.controller;

import com.rookies6.myspringboot4project.user.dto.UserDTO;
import com.rookies6.myspringboot4project.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 1. 전체 유저 조회
    @GetMapping
    public ResponseEntity<List<UserDTO.Response>> getAllUsers() {
        List<UserDTO.Response> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // 2. ID로 유저 조회
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO.Response> getUserById(
            @PathVariable Long id) {

        UserDTO.Response user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    // 3. 이메일로 유저 조회 (기존 학번 검색 대체)
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO.Response> getUserByEmail(
            @PathVariable String email) {

        UserDTO.Response user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    // 4. 유저 생성 (회원가입)
    @PostMapping
    public ResponseEntity<UserDTO.Response> createUser(
            @Valid @RequestBody UserDTO.Request request) {

        UserDTO.Response createdUser = userService.createUser(request);
        return ResponseEntity.ok(createdUser);
    }

    // 5. 유저 수정
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO.Response> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO.Request request) {

        UserDTO.Response updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    // 6. 유저 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {

        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}