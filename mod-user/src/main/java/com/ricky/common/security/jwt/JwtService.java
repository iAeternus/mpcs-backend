package com.ricky.common.security.jwt;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.properties.JwtProperties;
import com.ricky.common.security.MpcsAuthenticationToken;
import com.ricky.user.domain.User;
import com.ricky.user.domain.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateJwt(String userId) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + jwtProperties.getExpire() * 60L * 1000L);
        return generateJwt(userId, expirationDate);
    }

    public String generateJwt(String userId, Date expirationDate) {
        return Jwts.builder()
                .subject(userId)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(new Date())
                .expiration(expirationDate)
                .signWith(getSecretKey())
                .compact();
    }

    public MpcsAuthenticationToken tokenFrom(String jwt) {
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();

        String userId = claims.getSubject();
        User user = userRepository.cachedById(userId);
        user.checkActive();
        long expiration = claims.getExpiration().getTime();
        return new MpcsAuthenticationToken(UserContext.of(userId, user.getUsername(), user.getRole()), expiration);
    }
}
