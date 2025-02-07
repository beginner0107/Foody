package svsite.matzip.foody.domain.auth.api;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import svsite.matzip.foody.domain.auth.ControllerTestSupport;
import svsite.matzip.foody.domain.auth.api.dto.request.AuthRequestDto;
import svsite.matzip.foody.domain.auth.api.dto.response.TokenResponseDto;
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
}
