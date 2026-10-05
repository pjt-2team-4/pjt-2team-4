package com.rookies6.myspringboot4project.user.security.jwt;

import com.rookies6.myspringboot4project.user.entity.User;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;


@Component
public class JwtTokenProvider {

    private final SecretKey key;

    private final long accessExpirationMs =
            30 * 60 * 1000L;


    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret
    ) {

        this.key =
                Keys.hmacShaKeyFor(
                        secret.getBytes(
                                StandardCharsets.UTF_8
                        )
                );
    }


    // ==========================================
    // Access Token 만료시간
    // ==========================================

    public long getAccessExpirationSeconds() {

        return accessExpirationMs / 1000;
    }


    // ==========================================
    // Access Token 생성
    // ==========================================

    public String generateAccessToken(User user) {

        Date now = new Date();


        return Jwts.builder()

                .setSubject(
                        user.getEmail()
                )

                .setIssuedAt(now)

                .setExpiration(
                        new Date(
                                now.getTime()
                                        + accessExpirationMs
                        )
                )

                .signWith(
                        key,
                        SignatureAlgorithm.HS256
                )

                .compact();
    }


    // ==========================================
    // JWT 검증
    // ==========================================

    public boolean validateToken(
            String token
    ) {

        try {

            Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            return true;

        } catch (
                JwtException
                        | IllegalArgumentException e
        ) {

            return false;
        }
    }


    // ==========================================
    // JWT → 이메일
    // ==========================================

    public String getEmailFromToken(
            String token
    ) {

        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
