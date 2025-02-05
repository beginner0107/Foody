package svsite.matzip.foody.global.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static svsite.matzip.foody.global.exception.errorCode.ErrorCodes.USER_NOT_FOUND;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.domain.auth.repository.UserRepository;
import svsite.matzip.foody.global.exception.support.CustomException;
import svsite.matzip.foody.global.util.jwt.JwtTokenType;
import svsite.matzip.foody.global.util.jwt.JwtUtil;

@ExtendWith(MockitoExtension.class)
class AuthenticatedUserResolverTest {

  @InjectMocks
  private AuthenticatedUserResolver resolver;

  @Mock
  private JwtUtil jwtUtil;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private NativeWebRequest webRequest;

  @Mock
  private ModelAndViewContainer mavContainer;  // 추가
  @Mock
  private WebDataBinderFactory binderFactory;  // 추가

  private MethodParameter methodParameter;

  @BeforeEach
  void setup() throws NoSuchMethodException {
    methodParameter = new MethodParameter(
        this.getClass().getDeclaredMethod("dummyMethod", User.class), 0
    );
  }

  private void dummyMethod(@AuthenticatedUser(JwtTokenType.REFRESH) User user) {
  }


  @Test
  @DisplayName("토큰이 없을 경우 CustomException을 발생시킨다.")
  void resolveArgument_missingToken() {
    // given
    when(webRequest.getHeader("Authorization")).thenReturn(null);

    // when & then
    CustomException exception = assertThrows(CustomException.class, () ->
        resolver.resolveArgument(methodParameter, null, webRequest, null)
    );
    assertEquals("Authorization 헤더가 없거나 형식이 잘못되었습니다.", exception.getMessage());
  }

  @Test
  @DisplayName("잘못된 형식의 토큰이 들어오면 CustomException을 발생시킨다.")
  void resolveArgument_invalidTokenFormat() {
    // given
    when(webRequest.getHeader("Authorization")).thenReturn("InvalidToken");

    // when & then
    CustomException exception = assertThrows(CustomException.class, () ->
        resolver.resolveArgument(methodParameter, null, webRequest, null)
    );
    assertEquals("Authorization 헤더가 없거나 형식이 잘못되었습니다.", exception.getMessage());
  }

  @Test
  @DisplayName("유효한 토큰과 올바른 사용자 정보가 있을 경우 User 객체를 반환한다.")
  void resolveArgument_success() {
    // given
    String token = "validToken";
    Claims claims = Jwts.claims().setSubject("test@example.com");
    claims.put("type", JwtTokenType.REFRESH.name());

    when(webRequest.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtUtil.validateToken(token)).thenReturn(claims);
    when(userRepository.findByEmail("test@example.com")).thenReturn(
        Optional.of(User.builder()
            .email("test@example.com")
            .hashedRefreshToken("encodedValidRefreshToken")  // RefreshToken 설정
            .build())
    );
    when(passwordEncoder.matches(token, "encodedValidRefreshToken")).thenReturn(true);  // 매칭 설정

    // when
    User result = (User) resolver.resolveArgument(methodParameter, null, webRequest, null);

    // then
    assertNotNull(result);
    assertEquals("test@example.com", result.getEmail());
  }


  @Test
  @DisplayName("토큰 타입이 잘못된 경우 CustomException을 발생시킨다.")
  void resolveArgument_invalidTokenType() {
    // given
    String token = "validToken";
    Claims claims = Jwts.claims().setSubject("test@example.com");
    claims.put("type", JwtTokenType.ACCESS.name());  // 잘못된 타입

    when(webRequest.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtUtil.validateToken(token)).thenReturn(claims);

    // when & then
    CustomException exception = assertThrows(CustomException.class, () ->
        resolver.resolveArgument(methodParameter, null, webRequest, null)
    );
    assertEquals("Invalid token type. Expected: REFRESH", exception.getMessage());
  }

  @Test
  @DisplayName("사용자가 존재하지 않으면 CustomException을 발생시킨다.")
  void resolveArgument_userNotFound() {
    // given
    String token = "validToken";
    Claims claims = Jwts.claims().setSubject("notfound@example.com");
    claims.put("type", JwtTokenType.REFRESH.name());

    when(webRequest.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtUtil.validateToken(token)).thenReturn(claims);
    when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

    // when & then
    CustomException exception = assertThrows(CustomException.class, () ->
        resolver.resolveArgument(methodParameter, null, webRequest, null)
    );
    assertEquals(USER_NOT_FOUND, exception.getErrorCode());
  }
}
