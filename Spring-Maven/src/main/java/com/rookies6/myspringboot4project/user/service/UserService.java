package com.rookies6.myspringboot4project.user.service;

import com.rookies6.myspringboot4project.user.dto.UserDTO;
import com.rookies6.myspringboot4project.user.entity.User;
import com.rookies6.myspringboot4project.exception.BusinessException;
import com.rookies6.myspringboot4project.exception.ErrorCode;
import com.rookies6.myspringboot4project.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 1. 전체 유저 조회
    public List<UserDTO.Response> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserDTO.Response::fromEntity)
                .toList();
    }

    // 2. ID로 유저 조회
    public UserDTO.Response getUserById(Long id) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.RESOURCE_NOT_FOUND,
                                "User",
                                "id",
                                id
                        )
                );

        return UserDTO.Response.fromEntity(user);
    }

    // 3. 이메일로 유저 조회
    public UserDTO.Response getUserByEmail(String email) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.RESOURCE_NOT_FOUND,
                                "User",
                                "email",
                                email
                        )
                );

        return UserDTO.Response.fromEntity(user);
    }

    // 4. 유저 생성 (회원가입)
    @Transactional
    public UserDTO.Response createUser(UserDTO.Request request) {

        // 이메일 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(
                    ErrorCode.EMAIL_DUPLICATE,
                    request.getEmail()
            );
        }

        // User 생성 (email, password만 사용)
        User user = User.builder()
                .email(request.getEmail())
                .password(request.getPassword()) // 추후 Spring Security 적용 시 암호화(BCrypt) 필요
                .build();

        User savedUser = userRepository.save(user);

        return UserDTO.Response.fromEntity(savedUser);
    }

    // 5. 유저 정보 수정
    @Transactional
public UserDTO.Response updateUser(
        Long id,
        UserDTO.Request request
) {

    User user = userRepository
            .findById(id)
            .orElseThrow(() ->
                    new BusinessException(
                            ErrorCode.RESOURCE_NOT_FOUND,
                            "User",
                            "id",
                            id
                    )
            );

    String email = request.getEmail()
            .trim()
            .toLowerCase();

    if (!user.getEmail().equals(email)
            && userRepository.existsByEmail(email)) {

        throw new BusinessException(
                ErrorCode.EMAIL_DUPLICATE,
                email
        );
    }

    user.changeEmail(email);

    user.changePassword(
            passwordEncoder.encode(
                    request.getPassword()
            )
    );

    return UserDTO.Response.fromEntity(user);
}


    // 6. 유저 삭제
    @Transactional
    public void deleteUser(Long id) {

        if (!userRepository.existsById(id)) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "User",
                    "id",
                    id
            );
        }

        userRepository.deleteById(id);
    }

  
}