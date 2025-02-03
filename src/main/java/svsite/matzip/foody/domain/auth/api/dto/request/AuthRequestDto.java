package svsite.matzip.foody.domain.auth.api.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AuthRequestDto(
    @NotEmpty
    @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "이메일 형식이 아닙니다.")
    @Size(min = 6, max = 50)
    String email,

    @NotEmpty
    @Size(min = 8, max = 20)
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "비밀번호가 영어와 숫자 조합이 아닙니다.")
    String password
) {}
