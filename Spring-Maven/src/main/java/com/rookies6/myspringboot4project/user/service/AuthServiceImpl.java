package com.rookies6.myspringboot4project.user.service;

import com.rookies6.myspringboot4project.exception.BusinessException;
import com.rookies6.myspringboot4project.exception.ErrorCode;
import com.rookies6.myspringboot4project.security.JwtTokenProvider;
import com.rookies6.myspringboot4project.user.dto.LoginDTO;
import com.rookies6.myspringboot4project.user.dto.SignupDTO;
import com.rookies6.myspringboot4project.user.dto.TokenDTO;
import com.rookies6.myspringboot4project.user.entity.User;
import com.rookies6.myspringboot4project.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입
     */
    @Override
    @Transactional
    public SignupDTO.Response signup(SignupDTO.Request request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException(
                    ErrorCode.EMAIL_DUPLICATE,
                    email
            );
        }

        User user = User.builder()
                .email(email)
                .password(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )
                .build();

        try {
            User savedUser = userRepository.save(user);

            return SignupDTO.Response.fromEntity(savedUser);

        } catch (DataIntegrityViolationException e) {

            throw new BusinessException(
                    ErrorCode.EMAIL_DUPLICATE,
                    email
            );
        }
    }

    /**
     * 로그인
     */
    @Override
    public TokenDTO.Response login(LoginDTO.Request request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.RESOURCE_NOT_FOUND,
                                "User",
                                "email",
                                email
                        )
                );

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new BusinessException(
                    ErrorCode.INVALID_PASSWORD
            );
        }

        String accessToken =
                jwtTokenProvider.createToken(user.getEmail());

        return TokenDTO.Response.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .email(user.getEmail())
                .build();

    }
}
