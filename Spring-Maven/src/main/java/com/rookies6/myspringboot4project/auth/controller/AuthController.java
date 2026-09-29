package com.rookies6.myspringboot4project.auth.controller;

import com.rookies6.myspringboot4project.auth.dto.SignupDTO;
import com.rookies6.myspringboot4project.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignupDTO.Response> signup(
            @Valid @RequestBody SignupDTO.Request request) {

        SignupDTO.Response createdUser = authService.signup(request);
        return ResponseEntity.ok(createdUser);
    }
}
