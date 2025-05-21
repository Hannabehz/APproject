package util;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;


public class JwtUtil {
    private static final String SECRET_KEY = String.valueOf(Keys.hmacShaKeyFor(("MySuperSecretKeyMySuperSecretKey".getBytes())));
    private static final Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    private static final long EXPIRATION_TIME = 120*60*1000*10;
    public static String generateToken(String userName) {
        return Jwts.builder().setSubject(userName)
                .setIssuedAt(new Date())
                .setExpiration(new Date (System.currentTimeMillis()+EXPIRATION_TIME))
                .signWith(key,SignatureAlgorithm.HS256).compact();
    }
    public static String validateToken(String token) {
        try{
            return Jwts.parserBuilder().setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        }
        catch (JwtException e){
            return null;
        }
    }
    public static Key getkey() {
        return key;
    }
}
