package svsite.matzip.foody.domain.auth.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record UpdateCategoryDto(
    @Schema(description = "RED 카테고리", example = "한식 맛집")
    String red,

    @Schema(description = "BLUE 카테고리", example = "양식 맛집")
    String blue,

    @Schema(description = "GREEN 카테고리", example = "채식 맛집")
    String green,

    @Schema(description = "YELLOW 카테고리", example = "중식 맛집")
    String yellow,

    @Schema(description = "PURPLE 카테고리", example = "디저트 맛집")
    String purple
) {}
