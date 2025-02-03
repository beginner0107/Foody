package svsite.matzip.foody.domain.auth.api.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 요청 DTO")
public record AuthRequestDto(
    @Schema(
        description = "사용자 이메일",
        example = "test@example.com",
        minLength = 6,
        maxLength = 50,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty
    @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "이메일 형식이 아닙니다.")
    String email,

    @Schema(
        description = "사용자 비밀번호",
        example = "password123",
        minLength = 8,
        maxLength = 20,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty
    @Size(min = 8, max = 20)
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "비밀번호가 영어와 숫자 조합이 아닙니다.")
    String password
) {}
