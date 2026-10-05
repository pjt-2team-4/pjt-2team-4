package com.rookies6.myspringboot4project.config;

import com.rookies6.myspringboot4project.user.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;


    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                // ==========================================
                // CSRF
                // ==========================================
                .csrf(AbstractHttpConfigurer::disable)


                // ==========================================
                // 기본 로그인 / HTTP Basic 비활성화
                // ==========================================
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)


                // ==========================================
                // URL 접근 권한
                // ==========================================
                .authorizeHttpRequests(auth -> auth

                        // 회원가입 / 로그인
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/error"
                        ).permitAll()

                        // 나머지는 JWT 인증 필요
                        .anyRequest().authenticated()
                )


                // ==========================================
                // JWT Filter
                // ==========================================
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );


        return http.build();
    }
}
