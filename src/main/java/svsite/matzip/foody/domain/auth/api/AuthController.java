package svsite.matzip.foody.domain.auth.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import svsite.matzip.foody.domain.auth.api.dto.request.AuthRequestDto;
import svsite.matzip.foody.domain.auth.service.AuthService;

@Tag(name = "Auth", description = "회원 인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

  private final AuthService authService;

  @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "회원가입 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "409", description = "중복된 이메일")
  })
  @PostMapping("/signup")
  public ResponseEntity<Long> signup(@RequestBody @Valid AuthRequestDto authRequestDto) {
    return ResponseEntity.ok(authService.signup(authRequestDto));
  }
}
