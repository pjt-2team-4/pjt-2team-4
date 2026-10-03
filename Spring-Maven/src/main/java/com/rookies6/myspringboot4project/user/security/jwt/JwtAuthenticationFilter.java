package com.rookies6.myspringboot4project.user.security.jwt;

import com.rookies6.myspringboot4project.config.userinfo.UserInfoUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {


    private static final String BEARER_PREFIX = "Bearer ";


    private final JwtTokenProvider jwtTokenProvider;

    private final UserInfoUserDetailsService userInfoUserDetailsService;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {


        String authorizationHeader =
                request.getHeader(HttpHeaders.AUTHORIZATION);


        System.out.println(
                "[JWT] "
                        + request.getMethod()
                        + " "
                        + request.getRequestURI()
        );


        System.out.println(
                "[JWT] Authorization = "
                        + authorizationHeader
        );


        // ==========================================
        // Authorization Header 없음
        // ==========================================

        if (
                authorizationHeader == null
                        || !authorizationHeader.startsWith(BEARER_PREFIX)
        ) {

            System.out.println(
                    "[JWT] Bearer token 없음"
            );

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        // ==========================================
        // Bearer 제거
        // ==========================================

        String token =
                authorizationHeader.substring(
                        BEARER_PREFIX.length()
                );


        // ==========================================
        // JWT 검증
        // ==========================================

        if (!jwtTokenProvider.validateToken(token)) {

            System.out.println(
                    "[JWT] JWT 검증 실패"
            );

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        try {

            // ==========================================
            // JWT에서 이메일 추출
            // ==========================================

            String email =
                    jwtTokenProvider.getEmailFromToken(token);


            System.out.println(
                    "[JWT] 인증 사용자 = "
                            + email
            );


            // ==========================================
            // DB에서 사용자 조회
            // ==========================================

            UserDetails userDetails =
                    userInfoUserDetailsService
                            .loadUserByUsername(email);


            // ==========================================
            // Authentication 생성
            // ==========================================

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );


            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );


            // ==========================================
            // SecurityContext 등록
            // ==========================================

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);


            System.out.println(
                    "[JWT] SecurityContext 인증 등록 완료"
            );


        } catch (Exception e) {

            System.out.println(
                    "[JWT] 인증 처리 실패: "
                            + e.getMessage()
            );


            SecurityContextHolder
                    .clearContext();
        }


        // ==========================================
        // 다음 Filter
        // ==========================================

        filterChain.doFilter(
                request,
                response
        );
    }
}
