package svsite.matzip.foody.domain.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import svsite.matzip.foody.domain.auth.api.dto.request.AuthRequestDto;
import svsite.matzip.foody.domain.auth.api.dto.response.TokenResponseDto;
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.domain.auth.repository.UserRepository;
import svsite.matzip.foody.global.exception.errorCode.ErrorCodes;
import svsite.matzip.foody.global.exception.support.CustomException;
import svsite.matzip.foody.global.util.jwt.JwtUtil;


class AuthServiceTest {

  private final UserRepository userRepository = Mockito.mock(UserRepository.class);
  private final PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
  private final JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
  private final AuthService authService = new AuthService(userRepository, passwordEncoder, jwtUtil);

  @DisplayName("정상 회원가입")
  @Test
  void signup_success() {
    // given
    AuthRequestDto authRequestDto = new AuthRequestDto("test@example.com", "password123");

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

    doAnswer(invocation -> {
      User user = invocation.getArgument(0);
      ReflectionTestUtils.setField(user, "id", 1L);
      return user;
    }).when(userRepository).save(any(User.class));

    // when
    Long returnedId = authService.signup(authRequestDto);

    // then
    assertNotNull(returnedId, "저장된 User의 id는 null 이 아니어야 합니다.");
    assertEquals(1L, returnedId, "저장된 User의 id가 예상한 값이어야 합니다.");
    verify(userRepository).findByEmail("test@example.com");
    verify(passwordEncoder).encode("password123");
    verify(userRepository).save(any(User.class));
  }

  @DisplayName("중복 이메일로 회원가입시 예외 발생")
  @Test
  void signup_duplicateEmail_throwsException() {
    // given
    AuthRequestDto authRequestDto = new AuthRequestDto("duplicate@example.com", "password123");
    User existingUser = User.builder().build();
    when(userRepository.findByEmail("duplicate@example.com")).thenReturn(Optional.of(existingUser));

    // when & then
    CustomException exception = assertThrows(CustomException.class, () -> {
      authService.signup(authRequestDto);
    });
    assertEquals(ErrorCodes.USER_EMAIL_DUPLICATE, exception.getErrorCode());
    verify(userRepository).findByEmail("duplicate@example.com");
    verify(passwordEncoder, never()).encode(anyString());
    verify(userRepository, never()).save(any(User.class));
  }

  @DisplayName("로그인 성공")
  @Test
  void signin_success() {
    // given
    AuthRequestDto authRequestDto = new AuthRequestDto("test@example.com", "password123");
    User user = User.builder().password("encodedPassword").build();
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
    when(jwtUtil.generateAccessToken(anyMap())).thenReturn("accessToken");
    when(jwtUtil.generateRefreshToken(anyMap())).thenReturn("refreshToken");

    // when
    TokenResponseDto tokenResponse = authService.signin(authRequestDto);

    // then
    assertNotNull(tokenResponse, "토큰 응답이 null이 아니어야 합니다.");
    assertEquals("accessToken", tokenResponse.accessToken(), "Access token이 예상한 값이어야 합니다.");
    assertEquals("refreshToken", tokenResponse.refreshToken(), "Refresh token이 예상한 값이어야 합니다.");
    verify(userRepository).findByEmail("test@example.com");
    verify(passwordEncoder).matches("password123", "encodedPassword");
    verify(jwtUtil).generateAccessToken(anyMap());
    verify(jwtUtil).generateRefreshToken(anyMap());
  }

  @DisplayName("로그인 실패 - 잘못된 비밀번호")
  @Test
  void signin_wrongPassword_throwsException() {
    // given
    AuthRequestDto authRequestDto = new AuthRequestDto("test@example.com", "wrongPassword");
    User user = User.builder().password("encodedPassword").build();
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

    // when & then
    CustomException exception = assertThrows(CustomException.class, () -> authService.signin(authRequestDto));
    assertEquals(ErrorCodes.USER_WRONG_PASSWORD, exception.getErrorCode());
    verify(userRepository).findByEmail("test@example.com");
    verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
    verify(jwtUtil, never()).generateAccessToken(anyMap());
    verify(jwtUtil, never()).generateRefreshToken(anyMap());
  }

  @DisplayName("로그인 실패 - 사용자가 존재하지 않음")
  @Test
  void signin_emailNotFound_throwsException() {
    // given
    AuthRequestDto authRequestDto = new AuthRequestDto("notfound@example.com", "password123");
    when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

    // when & then
    CustomException exception = assertThrows(CustomException.class, () -> authService.signin(authRequestDto));
    assertEquals(ErrorCodes.USER_NOT_FOUND, exception.getErrorCode());
    verify(userRepository).findByEmail("notfound@example.com");
    verify(passwordEncoder, never()).matches(anyString(), anyString());
    verify(jwtUtil, never()).generateAccessToken(anyMap());
    verify(jwtUtil, never()).generateRefreshToken(anyMap());
  }
}
