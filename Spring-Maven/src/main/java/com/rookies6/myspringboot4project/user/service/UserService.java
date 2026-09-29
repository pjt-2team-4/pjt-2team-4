package com.rookies6.myspringboot4project.user.service;

import com.rookies6.myspringboot4project.user.dto.UserDTO;
import com.rookies6.myspringboot4project.user.entity.User;
import com.rookies6.myspringboot4project.exception.BusinessException;
import com.rookies6.myspringboot4project.exception.ErrorCode;
import com.rookies6.myspringboot4project.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

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

    // 3. 이메일로 유저 조회 (기존 학번 조회 대체)
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
                    ErrorCode.EMAIL_DUPLICATE, // 기존 STUDENT_NUMBER_DUPLICATE 대체
                    request.getEmail()
            );
        }

        // User 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(request.getPassword()) // 추후 Spring Security 적용 시 암호화(BCrypt) 필요
                .build();

        User savedUser = userRepository.save(user);

        return UserDTO.Response.fromEntity(savedUser);
    }

    // 5. 유저 정보 수정
    @Transactional
    public UserDTO.Response updateUser(Long id, UserDTO.Request request) {

        // 기존 유저 조회
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

        // 변경하려는 이메일이 기존과 다를 경우에만 중복 검사
        if (!user.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {

            throw new BusinessException(
                    ErrorCode.EMAIL_DUPLICATE,
                    request.getEmail()
            );
        }

        // 유저 정보 수정 (JPA Dirty Checking으로 자동 업데이트)
        user.changeEmail(request.getEmail());
        user.changePassword(request.getPassword());

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