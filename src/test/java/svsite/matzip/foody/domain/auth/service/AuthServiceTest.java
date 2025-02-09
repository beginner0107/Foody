package svsite.matzip.foody.domain.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import svsite.matzip.foody.domain.auth.api.dto.request.AuthRequestDto;
import svsite.matzip.foody.domain.auth.api.dto.request.EditProfileDto;
import svsite.matzip.foody.domain.auth.api.dto.response.ProfileResponseDto;
import svsite.matzip.foody.domain.auth.api.dto.response.TokenResponseDto;
import svsite.matzip.foody.domain.auth.entity.LoginType;
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.domain.auth.repository.UserRepository;
import svsite.matzip.foody.global.exception.errorCode.ErrorCodes;
import svsite.matzip.foody.global.exception.support.CustomException;
import svsite.matzip.foody.global.util.jwt.JwtUtil;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @InjectMocks
  private AuthService authService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtUtil jwtUtil;

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
    CustomException exception = assertThrows(CustomException.class,
        () -> authService.signin(authRequestDto));
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
    CustomException exception = assertThrows(CustomException.class,
        () -> authService.signin(authRequestDto));
    assertEquals(ErrorCodes.USER_NOT_FOUND, exception.getErrorCode());
    verify(userRepository).findByEmail("notfound@example.com");
    verify(passwordEncoder, never()).matches(anyString(), anyString());
    verify(jwtUtil, never()).generateAccessToken(anyMap());
    verify(jwtUtil, never()).generateRefreshToken(anyMap());
  }

  @Test
  @DisplayName("refreshToken이 유효하여 토큰이 재발급된다.")
  void refreshToken_success() {
    // given
    User mockUser = User.builder()
        .email("test@example.com")
        .hashedRefreshToken("existingHashedToken")
        .build();

    Map<String, Object> payload = new HashMap<>();
    payload.put("email", "test@example.com");

    String newAccessToken = "newAccessToken";
    String newRefreshToken = "newRefreshToken";

    // Mock 설정
    when(jwtUtil.generateAccessToken(payload)).thenReturn(newAccessToken);
    when(jwtUtil.generateRefreshToken(payload)).thenReturn(newRefreshToken);
    when(passwordEncoder.encode(newRefreshToken)).thenReturn("newEncodedRefreshToken");

    // when
    TokenResponseDto result = authService.refreshToken(mockUser);

    // then
    assertNotNull(result, "토큰 응답은 null이 아니어야 합니다.");
    assertNotNull(result.accessToken(), "AccessToken은 null이 아니어야 합니다.");
    assertNotNull(result.refreshToken(), "RefreshToken은 null이 아니어야 합니다.");

    // verify
    verify(jwtUtil).generateAccessToken(payload);
    verify(jwtUtil).generateRefreshToken(payload);
    verify(passwordEncoder).encode(newRefreshToken);
  }

  @Test
  @DisplayName("프로필 조회 성공 시 모든 사용자 정보를 반환한다.")
  void getProfile_success() {
    // given
    User mockUser = User.builder()
        .id(1L)
        .loginType(LoginType.KAKAO)
        .email("test@example.com")
        .nickname("테스터")
        .imageUri("https://example.com/image.jpg")
        .kakaoImageUri("https://kakao.com/profile.jpg")
        .YELLOW("맛있는 중국집")
        .GREEN("맛있는 수제 햄버거")
        .BLUE("맛있는 해물라면")
        .RED("정말 1티어 맛집")
        .PURPLE("부모님이 좋아하시는 한식")
        .build();

    // when
    ProfileResponseDto responseDto = authService.getProfile(mockUser);

    // then
    assertNotNull(responseDto, "프로필 응답은 null이 아니어야 합니다.");
    assertEquals(1L, responseDto.id(), "ID가 예상 값과 일치해야 합니다.");
    assertEquals(LoginType.KAKAO, responseDto.loginType(), "로그인 유형이 예상 값과 일치해야 합니다.");
    assertEquals("test@example.com", responseDto.email(), "이메일이 예상 값과 일치해야 합니다.");
    assertEquals("테스터", responseDto.nickname(), "닉네임이 예상 값과 일치해야 합니다.");
    assertEquals("https://example.com/image.jpg", responseDto.imageUri(), "프로필 이미지 URI가 예상 값과 일치해야 합니다.");
    assertEquals("https://kakao.com/profile.jpg", responseDto.kakaoImageUri(), "카카오 프로필 이미지 URI가 예상 값과 일치해야 합니다.");
    assertEquals("맛있는 중국집", responseDto.YELLOW(), "YELLOW 카테고리가 예상 값과 일치해야 합니다.");
    assertEquals("맛있는 수제 햄버거", responseDto.GREEN(), "GREEN 카테고리가 예상 값과 일치해야 합니다.");
    assertEquals("맛있는 해물라면", responseDto.BLUE(), "BLUE 카테고리가 예상 값과 일치해야 합니다.");
    assertEquals("정말 1티어 맛집", responseDto.RED(), "RED 카테고리가 예상 값과 일치해야 합니다.");
    assertEquals("부모님이 좋아하시는 한식", responseDto.PURPLE(), "PURPLE 카테고리가 예상 값과 일치해야 합니다.");
  }

  @Test
  @DisplayName("프로필 수정 시 성공적으로 수정된 프로필 정보를 반환한다.")
  void editProfile_success() {
    // given
    User mockUser = User.builder()
        .id(1L)
        .email("test@example.com")
        .nickname("기존 닉네임")
        .imageUri("https://example.com/original-profile.jpg")
        .loginType(LoginType.KAKAO)
        .build();

    EditProfileDto editProfileDto = new EditProfileDto(
        "수정된 닉네임",
        "https://example.com/new-profile.jpg"
    );

    // when
    ProfileResponseDto responseDto = authService.editProfile(editProfileDto, mockUser);

    // then
    assertNotNull(responseDto, "응답은 null이 아니어야 합니다.");
    assertEquals(mockUser.getId(), responseDto.id(), "ID가 예상 값과 일치해야 합니다.");
    assertEquals(editProfileDto.nickname(), responseDto.nickname(), "닉네임이 예상 값과 일치해야 합니다.");
    assertEquals(editProfileDto.imageUri(), responseDto.imageUri(), "이미지 URI가 예상 값과 일치해야 합니다.");

    verify(userRepository, never()).save(any(User.class));
  }
}
