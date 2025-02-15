package svsite.matzip.foody.domain.auth.api;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import svsite.matzip.foody.domain.auth.ControllerTestSupport;
import svsite.matzip.foody.domain.auth.api.dto.request.AuthRequestDto;
import svsite.matzip.foody.domain.auth.api.dto.request.EditProfileDto;
import svsite.matzip.foody.domain.auth.api.dto.request.UpdateCategoryDto;
import svsite.matzip.foody.domain.auth.api.dto.response.ProfileResponseDto;
import svsite.matzip.foody.domain.auth.api.dto.response.TokenResponseDto;
import svsite.matzip.foody.domain.auth.entity.LoginType;
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.global.exception.errorCode.ErrorCodes;
import svsite.matzip.foody.global.exception.support.CustomException;

class AuthControllerTest extends ControllerTestSupport {

  @Test
  @DisplayName("회원가입 성공 시 200 OK와 userId를 반환한다.")
  void signup_success() throws Exception {
    // given
    AuthRequestDto requestDto = new AuthRequestDto("test@example.com", "12345678");
    given(authService.signup(any(AuthRequestDto.class))).willReturn(1L);

    // when & then
    mockMvc.perform(post("/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isOk())
        .andExpect(content().string("1"));
  }

  @Test
  @DisplayName("중복된 이메일인 경우 409 CONFLICT를 반환한다.")
  void signup_duplicateEmail() throws Exception {
    // given
    AuthRequestDto requestDto = new AuthRequestDto("exists@example.com", "12345678");
    doThrow(new CustomException(ErrorCodes.USER_EMAIL_DUPLICATE))
        .when(authService).signup(any(AuthRequestDto.class));

    // when & then
    mockMvc.perform(post("/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isConflict()); // 409
  }

  @Test
  @DisplayName("유효성 검증에 걸리면 400 BAD REQUEST를 반환한다.")
  void signup_invalidRequest() throws Exception {
    AuthRequestDto invalidDto = new AuthRequestDto("", "");

    // when & then
    mockMvc.perform(post("/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidDto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("로그인 성공 시 200 OK와 토큰 정보를 반환한다")
  void signin_success() throws Exception {
    // given
    AuthRequestDto requestDto = new AuthRequestDto("test@example.com", "12345678");
    TokenResponseDto tokenResponse = new TokenResponseDto("mockAccessToken", "mockRefreshToken");

    given(authService.signin(any(AuthRequestDto.class))).willReturn(tokenResponse);

    // when & then
    mockMvc.perform(post("/auth/signin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken", is("mockAccessToken")))
        .andExpect(jsonPath("$.refreshToken", is("mockRefreshToken")));
  }

  @Test
  @DisplayName("존재하지 않는 사용자인 경우 404 NOT FOUND 반환")
  void signin_userNotFound() throws Exception {
    // given
    AuthRequestDto requestDto = new AuthRequestDto("notfound@example.com", "12345678");

    doThrow(new CustomException(ErrorCodes.USER_NOT_FOUND))
        .when(authService).signin(any(AuthRequestDto.class));

    // when & then
    mockMvc.perform(post("/auth/signin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isNotFound()); // 404
  }

  @Test
  @DisplayName("비밀번호 불일치 등 잘못된 요청 시 400 BAD REQUEST 반환")
  void signin_wrongPassword() throws Exception {
    // given
    AuthRequestDto requestDto = new AuthRequestDto("test@example.com", "wrongpw");

    doThrow(new CustomException(ErrorCodes.USER_WRONG_PASSWORD))
        .when(authService).signin(any(AuthRequestDto.class));

    // when & then
    mockMvc.perform(post("/auth/signin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isBadRequest()); // 400
  }

  @Test
  @DisplayName("토큰 재발급 성공 시 201 Created와 토큰 정보를 반환한다.")
  void refreshToken_success() throws Exception {
    // given
    User mockUser = User.builder()
        .email("test@example.com")
        .hashedRefreshToken("hashedRefreshToken")
        .build();

    TokenResponseDto tokenResponse = new TokenResponseDto("newAccessToken", "newRefreshToken");

    // 인증된 사용자 주입 및 서비스 동작 모의(Mock)
    when(authenticatedUserResolver.supportsParameter(any())).thenReturn(true);
    when(authenticatedUserResolver.resolveArgument(any(), any(), any(), any())).thenReturn(mockUser);
    when(authService.refreshToken(any(User.class))).thenReturn(tokenResponse);

    // when & then
    mockMvc.perform(get("/auth/refresh")
            .header(HttpHeaders.AUTHORIZATION, "Bearer validRefreshToken")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.accessToken", is("newAccessToken")))
        .andExpect(jsonPath("$.refreshToken", is("newRefreshToken")));
  }

  @Test
  @DisplayName("프로필 조회 성공 시 200 OK와 프로필 정보를 반환한다.")
  void getProfile_success() throws Exception {
    // given
    User mockUser = User.builder()
        .id(1L)
        .email("test@example.com")
        .nickname("테스터")
        .loginType(LoginType.KAKAO)
        .imageUri("https://example.com/profile.jpg")
        .build();

    ProfileResponseDto responseDto = ProfileResponseDto.builder()
        .id(mockUser.getId())
        .email(mockUser.getEmail())
        .nickname(mockUser.getNickname())
        .loginType(mockUser.getLoginType())
        .imageUri(mockUser.getImageUri())
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    when(authenticatedUserResolver.supportsParameter(any())).thenReturn(true);
    when(authenticatedUserResolver.resolveArgument(any(), any(), any(), any())).thenReturn(mockUser);
    when(authService.getProfile(any(User.class))).thenReturn(responseDto);

    // when & then
    mockMvc.perform(get("/auth/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer validAccessToken")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.nickname").value("테스터"))
        .andExpect(jsonPath("$.loginType").value("KAKAO"))
        .andExpect(jsonPath("$.imageUri").value("https://example.com/profile.jpg"));
  }


  @Test
  @DisplayName("프로필 수정 성공 시 200 OK와 수정된 프로필 정보를 반환한다.")
  void editProfile_success() throws Exception {
    // given
    User mockUser = User.builder()
        .id(1L)
        .email("test@example.com")
        .nickname("기존 닉네임")
        .build();

    EditProfileDto editProfileDto = EditProfileDto.builder()
        .nickname("수정된 닉네임")
        .imageUri("https://example.com/new-profile.jpg")
        .build();

    ProfileResponseDto responseDto = ProfileResponseDto.builder()
        .id(mockUser.getId())
        .email(mockUser.getEmail())
        .nickname(editProfileDto.nickname())
        .imageUri(editProfileDto.imageUri())
        .loginType(LoginType.KAKAO)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    when(authenticatedUserResolver.supportsParameter(any())).thenReturn(true);
    when(authenticatedUserResolver.resolveArgument(any(), any(), any(), any())).thenReturn(mockUser);
    when(authService.editProfile(any(EditProfileDto.class), any(User.class))).thenReturn(responseDto);

    // when & then
    mockMvc.perform(patch("/auth/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer validAccessToken")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(editProfileDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.nickname").value("수정된 닉네임"))
        .andExpect(jsonPath("$.imageUri").value("https://example.com/new-profile.jpg"))
        .andExpect(jsonPath("$.loginType").value("KAKAO"));

    verify(authService).editProfile(any(EditProfileDto.class), eq(mockUser));
  }

  @Test
  @DisplayName("계정 삭제 성공 시 204 No Content와 삭제된 사용자 ID를 반환한다.")
  void deleteAccount_success() throws Exception {
    // given
    User mockUser = User.builder()
        .id(1L)
        .email("test@example.com")
        .nickname("테스터")
        .build();

    // 인증된 사용자 주입 설정
    when(authenticatedUserResolver.supportsParameter(any())).thenReturn(true);
    when(authenticatedUserResolver.resolveArgument(any(), any(), any(), any())).thenReturn(mockUser);

    // 서비스 동작 모의(Mock)
    when(authService.deleteAccount(any(User.class))).thenReturn(mockUser.getId());

    // when & then
    mockMvc.perform(delete("/auth/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer validAccessToken")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent())
        .andExpect(jsonPath("$").value(1L)); // 응답이 사용자 ID인지 검증

    // 서비스 메서드 호출 검증
    verify(authService, times(1)).deleteAccount(mockUser);
  }

  @Test
  @DisplayName("카테고리 수정 시 200 OK와 수정된 프로필 정보를 반환한다.")
  void updateCategory_success() throws Exception {
    // given
    User mockUser = User.builder()
        .id(1L)
        .email("test@example.com")
        .nickname("테스터")
        .build();
    UpdateCategoryDto updateCategoryDto = new UpdateCategoryDto("한식", "양식", "채식", "중식", "디저트");

    ProfileResponseDto responseDto = ProfileResponseDto.builder()
        .id(mockUser.getId())
        .nickname(mockUser.getNickname())
        .RED(updateCategoryDto.red())
        .BLUE(updateCategoryDto.blue())
        .GREEN(updateCategoryDto.green())
        .YELLOW(updateCategoryDto.yellow())
        .PURPLE(updateCategoryDto.purple())
        .build();

    when(authService.updateCategory(any(UpdateCategoryDto.class), any(User.class))).thenReturn(responseDto);

    // when & then
    mockMvc.perform(patch("/auth/category")
            .header(HttpHeaders.AUTHORIZATION, "Bearer validAccessToken")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateCategoryDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(mockUser.getId()))
        .andExpect(jsonPath("$.RED").value("한식"))
        .andExpect(jsonPath("$.BLUE").value("양식"))
        .andExpect(jsonPath("$.GREEN").value("채식"))
        .andExpect(jsonPath("$.YELLOW").value("중식"))
        .andExpect(jsonPath("$.PURPLE").value("디저트"));
  }
}
