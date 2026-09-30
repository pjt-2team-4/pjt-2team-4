package com.rookies6.myspringboot4project.sec.auth.service;

import com.rookies6.myspringboot4project.sec.auth.dto.SignupDTO;
import com.rookies6.myspringboot4project.exception.BusinessException;
import com.rookies6.myspringboot4project.exception.ErrorCode;
import com.rookies6.myspringboot4project.sec.auth.entity.User;
import com.rookies6.myspringboot4project.sec.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public SignupDTO.Response signup(SignupDTO.Request request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(
                    ErrorCode.EMAIL_DUPLICATE,
                    email
            );
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        try {
            User savedUser = userRepository.save(user);
            return SignupDTO.Response.fromEntity(savedUser);
        } catch (DataIntegrityViolationException e) {
            // existsByEmail 체크 이후 동시에 같은 이메일로 가입 요청이 들어와
            // DB unique 제약조건(email)에서 충돌한 경우 -> 409로 변환
            throw new BusinessException(
                    ErrorCode.EMAIL_DUPLICATE,
                    email
            );
        }
    }
}
