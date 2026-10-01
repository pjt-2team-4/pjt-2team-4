package com.rookies6.myspringboot4project.user.controller;

import com.rookies6.myspringboot4project.user.dto.LoginDTO;
import com.rookies6.myspringboot4project.user.dto.SignupDTO;
import com.rookies6.myspringboot4project.user.dto.TokenDTO;
import com.rookies6.myspringboot4project.user.dto.UserDTO;
import com.rookies6.myspringboot4project.user.service.AuthService;
import com.rookies6.myspringboot4project.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    /**
     * 전체 회원 조회
     */
    @GetMapping
    public ResponseEntity<List<UserDTO.Response>> getAllUsers() {
        return ResponseEntity.ok(
                userService.getAllUsers()
        );
    }

    /**
     * 회원 단건 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO.Response> getUserById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                userService.getUserById(id)
        );
    }

    /**
     * 이메일로 회원 조회
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO.Response> getUserByEmail(
            @PathVariable String email
    ) {
        return ResponseEntity.ok(
                userService.getUserByEmail(email)
        );
    }

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<SignupDTO.Response> signup(
            @Valid @RequestBody SignupDTO.Request request
    ) {
        return ResponseEntity.ok(
                authService.signup(request)
        );
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<TokenDTO.Response> login(
            @Valid @RequestBody LoginDTO.Request request
    ) {
        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    /**
     * 회원 정보 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO.Response> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO.Request request
    ) {
        return ResponseEntity.ok(
                userService.updateUser(id, request)
        );
    }

    /**
     * 회원 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id
    ) {
        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }
}
