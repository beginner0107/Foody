package svsite.matzip.foody.domain.auth.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import svsite.matzip.foody.domain.auth.entity.LoginType;
import svsite.matzip.foody.domain.auth.entity.User;

@Builder
public record ProfileResponseDto(
    @Schema(description = "회원 ID", example = "1")
    Long id,

    @Schema(description = "로그인 유형", example = "KAKAO")
    LoginType loginType,

    @Schema(description = "이메일 주소", example = "test@example.com")
    String email,

    @Schema(description = "닉네임", example = "테스터")
    String nickname,

    @Schema(description = "프로필 이미지 URI", example = "https://example.com/image.jpg")
    String imageUri,

    @Schema(description = "카카오 프로필 이미지 URI", example = "https://kakao.com/profile.jpg")
    String kakaoImageUri,

    @Schema(description = "YELLOW 카테고리", example = "맛있는 중국집")
    String YELLOW,

    @Schema(description = "GREEN 카테고리", example = "맛있는 수제 햄버거")
    String GREEN,

    @Schema(description = "BLUE 카테고리", example = "맛있는 해물라면")
    String BLUE,

    @Schema(description = "RED 카테고리", example = "정말 1티어 맛집")
    String RED,

    @Schema(description = "PURPLE 카테고리", example = "부모님이 좋아하시는 한식")
    String PURPLE,

    @Schema(description = "계정 삭제일시", example = "2025-02-08T12:00:00")
    LocalDateTime deletedAt,

    @Schema(description = "계정 생성일시", example = "2023-01-01T12:00:00")
    LocalDateTime createdAt,

    @Schema(description = "계정 수정일시", example = "2023-06-01T12:00:00")
    LocalDateTime updatedAt
) {
  public static ProfileResponseDto from(User user) {
    return ProfileResponseDto.builder()
        .id(user.getId())
        .loginType(user.getLoginType())
        .email(user.getEmail())
        .nickname(user.getNickname())
        .imageUri(user.getImageUri())
        .kakaoImageUri(user.getKakaoImageUri())
        .YELLOW(user.getYELLOW())
        .GREEN(user.getGREEN())
        .BLUE(user.getBLUE())
        .RED(user.getRED())
        .PURPLE(user.getPURPLE())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .deletedAt(user.getDeletedAt())
        .build();
  }
}
