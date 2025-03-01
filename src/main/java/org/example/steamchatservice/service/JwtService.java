package org.example.steamchatservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {
    private final String SECRET_KEY = " mos403mvls9092dev270620SOE3438uz/anorbank/uz2025";

    public Mono<String> validateToken(String token) {
        return Mono.fromCallable(() -> extractUsername(token))
                .onErrorReturn("");
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        JwtParser jwtParser = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .build();

        Claims claims = jwtParser.parseSignedClaims(token).getPayload();
        return claimsResolver.apply(claims);
    }
}
