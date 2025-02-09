package svsite.matzip.foody.domain.favorite.api.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "즐겨찾기 토글 응답 DTO")
public record ToggleFavoriteResponseDto(

    @Schema(description = "게시글 ID", example = "1", requiredMode = REQUIRED)
    Long postId,

    @Schema(description = "즐겨찾기 등록 여부", example = "true", requiredMode = REQUIRED)
    boolean isFavorite
) {}
