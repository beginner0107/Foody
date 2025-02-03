package svsite.matzip.foody.domain.auth.api;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import svsite.matzip.foody.domain.auth.api.dto.request.AuthRequestDto;
import svsite.matzip.foody.domain.auth.service.AuthService;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

class AuthControllerTest {

  private final AuthService authService = Mockito.mock(AuthService.class);
  private final AuthController authController = new AuthController(authService);

  @Test
  void signup_shouldReturnUserId_whenValidRequest() {
    // given
    AuthRequestDto requestDto = new AuthRequestDto("test@example.com", "password123");
    when(authService.signup(requestDto)).thenReturn(1L);

    // when
    ResponseEntity<Long> response = authController.signup(requestDto);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(1L);
  }
}
