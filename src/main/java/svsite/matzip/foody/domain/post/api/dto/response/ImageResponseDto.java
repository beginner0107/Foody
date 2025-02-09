package svsite.matzip.foody.domain.post.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ImageResponseDto(
    @Schema(description = "이미지 ID", example = "10")
    Long id,
    @Schema(description = "이미지 URL", example = "https://example.com/images/1.jpg")
    String uri,
    @Schema(description = "이미지 생성 날짜 및 시간", example = "2025-02-08T12:00:00")
    LocalDateTime createdAt,
    @Schema(description = "이미지 수정 날짜 및 시간", example = "2025-02-08T13:00:00")
    LocalDateTime updatedAt,
    @Schema(description = "이미지 삭제 날짜 및 시간", example = "2025-02-09T12:00:00")
    LocalDateTime deletedAt
) {
}
