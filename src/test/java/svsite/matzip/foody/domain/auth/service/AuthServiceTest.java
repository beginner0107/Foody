package svsite.matzip.foody.domain.auth.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import org.springframework.test.util.ReflectionTestUtils;
import svsite.matzip.foody.domain.auth.api.dto.request.AuthRequestDto;
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.domain.auth.repository.UserRepository;
import svsite.matzip.foody.global.exception.errorCode.ErrorCodes;
import svsite.matzip.foody.global.exception.support.CustomException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


class AuthServiceTest {

  private final UserRepository userRepository = Mockito.mock(UserRepository.class);
  private final PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
  private final AuthService authService = new AuthService(userRepository, passwordEncoder);

  /**
   * 정상 회원가입 테스트
   */
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

  /**
   * 중복 이메일로 회원가입 시 예외 발생 테스트
   */
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
}
