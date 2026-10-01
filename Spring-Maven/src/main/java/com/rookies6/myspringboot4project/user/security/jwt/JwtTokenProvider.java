package com.rookies6.myspringboot4project.user.security.jwt;

import com.rookies6.myspringboot4project.user.entity.User;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final SecretKey key;
    private final long accessExpirationMs = 30 * 60 * 1000; // 30분

    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // 만료시간(초) - 응답 바디에 expiresIn으로 내려줄 때 사용
    public long getAccessExpirationSeconds() {
        return accessExpirationMs / 1000;
    }

    // 1. accessToken 생성 메서드
    public String generateAccessToken(User user) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(user.getEmail()) // 표준 클레임 - 이 토큰이 누구에 대한 것인지
                .setIssuedAt(now) // 토큰 발급 시각
                .setExpiration(new Date(now.getTime() + accessExpirationMs)) // 토큰 만료 시각
                .signWith(key, SignatureAlgorithm.HS256) // 토큰 서명
                .compact();
    }

    // 2. 토큰 검증 메서드
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 토큰에서 이메일 추출하는 메서드
    public String getEmailFromToken(String token) {
        return Jwts.parser().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

}
