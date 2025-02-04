package svsite.matzip.foody.global.util.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import svsite.matzip.foody.global.util.jwt.exception.InvalidJwtTokenException;

@Component
public class JwtUtil {

  private final long accessTokenExpiration;
  private final long refreshTokenExpiration;
  private final Key signingKey;
  private static final String TYPE_CLAIM = "type";
  private static final String EMAIL_CLAIM = "email";
  private static final String BEARER_TOKEN = "Bearer ";

  public JwtUtil(
      @Value("${jwt.secret}") String secretKey,
      @Value("${jwt.expiration.access-token}") long accessTokenExpiration,
      @Value("${jwt.expiration.refresh-token}") long refreshTokenExpiration) {
    this.accessTokenExpiration = accessTokenExpiration;
    this.refreshTokenExpiration = refreshTokenExpiration;
    byte[] decodedKey = Base64.getDecoder().decode(secretKey);
    this.signingKey = Keys.hmacShaKeyFor(decodedKey);
  }

  public String generateAccessToken(Map<String, Object> payload) {
    payload.put(TYPE_CLAIM, JwtTokenType.ACCESS);
    String email = (String) payload.get(EMAIL_CLAIM);

    return Jwts.builder()
        .setClaims(payload)
        .setSubject(email)
        .setExpiration(calculateExpiration(accessTokenExpiration))
        .signWith(signingKey, SignatureAlgorithm.HS256)
        .compact();
  }

  public String generateRefreshToken(Map<String, Object> payload) {
    payload.put(TYPE_CLAIM, JwtTokenType.REFRESH.name());
    String email = (String) payload.get(EMAIL_CLAIM);

    return Jwts.builder()
        .setClaims(payload)
        .setSubject(email)
        .setIssuedAt(new Date())
        .setExpiration(calculateExpiration(refreshTokenExpiration))
        .signWith(signingKey, SignatureAlgorithm.HS256)
        .compact();
  }

  // 토큰 검증
  public Claims validateToken(String token) {
    try {
      return Jwts.parserBuilder()
          .setSigningKey(signingKey)
          .build()
          .parseClaimsJws(token)
          .getBody();
    } catch (ExpiredJwtException e) {
      throw new InvalidJwtTokenException(JwtErrorMessages.TOKEN_EXPIRED, e);
    } catch (UnsupportedJwtException e) {
      throw new InvalidJwtTokenException(JwtErrorMessages.TOKEN_UNSUPPORTED, e);
    } catch (MalformedJwtException e) {
      throw new InvalidJwtTokenException(JwtErrorMessages.TOKEN_MALFORMED, e);
    } catch (SecurityException e) {
      throw new InvalidJwtTokenException(JwtErrorMessages.TOKEN_INVALID_SIGNATURE, e);
    } catch (JwtException e) {
      throw new InvalidJwtTokenException(JwtErrorMessages.TOKEN_INVALID, e);
    }
  }

  private String parseBearerToken(String token) {
    if (token == null || !token.startsWith(BEARER_TOKEN)) {
      throw new InvalidJwtTokenException(JwtErrorMessages.TOKEN_HEADER_INVALID);
    }
    return token.replace(BEARER_TOKEN, "").trim();
  }

  private Date calculateExpiration(long duration) {
    return new Date(System.currentTimeMillis() + duration);
  }
}
