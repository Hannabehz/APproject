package util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

public class JwtUtil {
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    public static String generateToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 ساعت
                .signWith(SECRET_KEY)
                .compact();
    }

    public static String validateToken(String token) throws JwtException {
        if (token == null || token.trim().isEmpty()) {
            System.err.println("Error: token is null or empty in validateToken");
            throw new JwtException("Token cannot be null or empty");
        }
        System.out.println("Validating JWT token: " + token);
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException e) {
            System.err.println("Error in validateToken: " + e.getMessage());
            throw e;
        }
    }
}