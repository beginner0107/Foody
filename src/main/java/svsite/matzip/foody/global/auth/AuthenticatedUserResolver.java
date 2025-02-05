package svsite.matzip.foody.global.auth;

import static svsite.matzip.foody.global.exception.errorCode.ErrorCodes.USER_NOT_FOUND;
import static svsite.matzip.foody.global.util.jwt.JwtErrorMessages.REFRESH_TOKEN_INVALID;

import io.jsonwebtoken.Claims;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.domain.auth.repository.UserRepository;
import svsite.matzip.foody.global.exception.support.CustomException;
import svsite.matzip.foody.global.util.jwt.JwtTokenType;
import svsite.matzip.foody.global.util.jwt.JwtUtil;
import svsite.matzip.foody.global.util.jwt.exception.InvalidJwtTokenException;

@Component
@RequiredArgsConstructor
public class AuthenticatedUserResolver implements HandlerMethodArgumentResolver {

  private final JwtUtil jwtUtil;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    AuthenticatedUser annotation = parameter.getParameterAnnotation(AuthenticatedUser.class);
    return annotation != null && parameter.getParameterType().equals(User.class);
  }

  @Override
  public @NonNull Object resolveArgument(@NonNull MethodParameter parameter,
      @Nullable ModelAndViewContainer mavContainer,
      @NonNull NativeWebRequest webRequest,
      @Nullable  WebDataBinderFactory binderFactory) {

    // Authorization 헤더에서 토큰 추출
    String token = webRequest.getHeader("Authorization");
    if (token == null || !token.startsWith("Bearer ")) {
      throw new CustomException("Authorization 헤더가 없거나 형식이 잘못되었습니다.");
    }
    token = token.substring(7);  // "Bearer " 제거

    // JWT 검증 및 클레임 추출
    Claims claims = jwtUtil.validateToken(token);
    String email = claims.getSubject();

    // 토큰 타입 검증
    AuthenticatedUser annotation = parameter.getParameterAnnotation(AuthenticatedUser.class);
    JwtTokenType requiredType = annotation.value();
    JwtTokenType tokenType = JwtTokenType.valueOf((String) claims.get("type"));

    if (requiredType != tokenType) {
      throw new CustomException("Invalid token type. Expected: " + requiredType);
    }

    // 사용자 조회
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

    // RefreshToken 검증이 필요한 경우 추가 검증
    if (requiredType == JwtTokenType.REFRESH) {
      String hashedRefreshToken = user.getHashedRefreshToken();
      if (hashedRefreshToken == null || hashedRefreshToken.isEmpty()) {
        throw new InvalidJwtTokenException(REFRESH_TOKEN_INVALID);
      }

      if (!passwordEncoder.matches(token, hashedRefreshToken)) {
        throw new CustomException(REFRESH_TOKEN_INVALID);
      }
    }

    return user;
  }
}
