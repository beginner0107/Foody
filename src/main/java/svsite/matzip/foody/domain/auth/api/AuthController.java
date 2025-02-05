package svsite.matzip.foody.domain.auth.api;

import static org.springframework.http.HttpStatus.CREATED;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import svsite.matzip.foody.domain.auth.api.dto.request.AuthRequestDto;
import svsite.matzip.foody.domain.auth.api.dto.response.TokenResponseDto;
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.domain.auth.service.AuthService;
import svsite.matzip.foody.global.auth.AuthenticatedUser;
import svsite.matzip.foody.global.util.jwt.JwtTokenType;

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

  @Operation(summary = "로그인", description = "사용자가 로그인 합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "회원가입 성공"),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청")
  })
  @PostMapping("/signin")
  public ResponseEntity<TokenResponseDto> signin(@RequestBody @Valid AuthRequestDto authRequestDto) {
    return ResponseEntity.ok().body(authService.signin(authRequestDto));
  }

  @Operation(summary = "엑세스, 리프레쉬 토큰 재발급", description = "엑세스 토큰 만료시 리프레쉬 토큰으로 재발급합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "엑세스, 리프레쉬 토큰 재발급 성공"),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청")
  })
  @GetMapping("/refresh")
  public ResponseEntity<TokenResponseDto> refresh(
      @AuthenticatedUser(JwtTokenType.REFRESH) User user) {
    TokenResponseDto tokens = authService.refreshToken(user);
    return ResponseEntity.status(HttpStatus.CREATED).body(tokens);
  }
}
