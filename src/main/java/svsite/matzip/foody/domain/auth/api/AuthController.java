package svsite.matzip.foody.domain.auth.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import svsite.matzip.foody.domain.auth.api.dto.request.AuthRequestDto;
import svsite.matzip.foody.domain.auth.api.dto.request.EditProfileDto;
import svsite.matzip.foody.domain.auth.api.dto.response.ProfileResponseDto;
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

  @Operation(summary = "엑세스, 리프레쉬 토큰 재발급",
      description = "엑세스 토큰 만료 시 리프레쉬 토큰으로 재발급합니다.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @GetMapping("/refresh")
  public ResponseEntity<TokenResponseDto> refresh(
      @AuthenticatedUser(JwtTokenType.REFRESH) User user) {
    TokenResponseDto tokens = authService.refreshToken(user);
    return ResponseEntity.status(HttpStatus.CREATED).body(tokens);
  }

  @Operation(
      summary = "로그아웃",
      description = "사용자를 로그아웃하고 리프레쉬 토큰을 삭제합니다.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @GetMapping("/logout")
  public ResponseEntity<Long> logout(@AuthenticatedUser User user) {
    return ResponseEntity.status(HttpStatus.OK).body(authService.deleteRefreshToken(user));
  }

  @Operation(
      summary = "내 프로필 조회",
      description = "로그인한 사용자의 프로필 정보를 조회합니다.",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
      @ApiResponse(responseCode = "401", description = "인증 실패")
  })
  @GetMapping("/me")
  public ResponseEntity<ProfileResponseDto> getProfile(@AuthenticatedUser User user) {
    return ResponseEntity.ok(authService.getProfile(user));
  }

  @Operation(
      summary = "프로필 수정",
      description = "사용자의 프로필 정보를 수정합니다.",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 실패")
  })
  @PatchMapping("/me")
  public ResponseEntity<ProfileResponseDto> editProfile(
      @AuthenticatedUser User user,
      @RequestBody @Valid EditProfileDto editProfileDto
  ) {
    return ResponseEntity.ok(authService.editProfile(editProfileDto, user));
  }
}
