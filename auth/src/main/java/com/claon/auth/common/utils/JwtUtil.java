package com.claon.auth.common.utils;

import com.claon.auth.common.domain.JwtDto;
import com.claon.auth.common.domain.RefreshToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final RefreshTokenUtil refreshTokenUtil;

    @Value("${spring.jwt.secret-key}")
    private String SECRET_KEY;

    @Value("${spring.jwt.access-token.expire-seconds}")
    private Long ACCESS_TOKEN_EXPIRE_TIME;

    @PostConstruct
    protected void init() {
        this.SECRET_KEY = Base64.getEncoder().encodeToString(this.SECRET_KEY.getBytes());
    }

    public JwtDto createToken(
            String userPk
    ) {
        Date now = new Date();
        return JwtDto.of(
                generateAccessToken(userPk, now),
                generateRefreshToken(userPk)
        );
    }

    public JwtDto reissueToken(
            String refreshToken
    ) {
        String userPk = this.refreshTokenUtil.delete(refreshToken);

        Date now = new Date();

        return JwtDto.of(
                generateAccessToken(userPk, now),
                generateRefreshToken(userPk)
        );
    }

    private String generateAccessToken(String userPk, Date now) {
        Claims claims = Jwts.claims().setSubject(userPk);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + this.ACCESS_TOKEN_EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS256, this.SECRET_KEY)
                .compact();
    }

    private String generateRefreshToken(String userPk) {
        RefreshToken refreshToken = RefreshToken.of(userPk);

        this.refreshTokenUtil.save(refreshToken);

        return refreshToken.getKey();
    }

    public void deleteRefreshToken(String refreshToken) {
        this.refreshTokenUtil.delete(refreshToken);
    }

    public String getUserId(String token) {
        return Jwts.parser().setSigningKey(this.SECRET_KEY).parseClaimsJws(token).getBody().getSubject();
    }
}