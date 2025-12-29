package com.ricky.security.jwt;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.properties.JwtProperties;
import com.ricky.security.MpcsAuthenticationToken;
import com.ricky.user.domain.User;
import com.ricky.user.domain.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

import static io.jsonwebtoken.SignatureAlgorithm.HS512;

@Component
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;

    public String generateJwt(String userId) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + jwtProperties.getExpire() * 60L * 1000L);
        return generateJwt(userId, expirationDate);
    }

    public String generateJwt(String userId, Date expirationDate) {
        Claims claims = Jwts.claims().setSubject(userId);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(HS512, jwtProperties.getSecret())
                .compact();
    }

    public MpcsAuthenticationToken tokenFrom(String jwt) {
        Claims claims = Jwts.parser().setSigningKey(jwtProperties.getSecret()).parseClaimsJws(jwt).getBody();
        String userId = claims.getSubject();
        User user = userRepository.cachedById(userId);
        user.checkActive();
        long expiration = claims.getExpiration().getTime();
        return new MpcsAuthenticationToken(UserContext.of(userId, user.getUsername(), user.getRole()), expiration);
    }
}
