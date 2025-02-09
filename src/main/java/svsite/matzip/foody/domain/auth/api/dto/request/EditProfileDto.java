package svsite.matzip.foody.domain.auth.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record EditProfileDto(
    @Schema(description = "닉네임 (1~20자)", example = "테스터")
    @NotEmpty
    @Size(min = 1, max = 20)
    String nickname,

    @Schema(description = "프로필 이미지 URI", example = "https://example.com/profile.jpg")
    String imageUri
) {}
