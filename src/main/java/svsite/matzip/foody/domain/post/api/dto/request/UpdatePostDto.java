package svsite.matzip.foody.domain.post.api.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import svsite.matzip.foody.domain.post.entity.MarkerColor;

@Schema(description = "맛집 글 수정 요청 DTO")
public record UpdatePostDto(

    @Schema(description = "마커 색상", example = "RED", requiredMode = REQUIRED)
    @NotNull(message = "마커 색상은 필수입니다.")
    MarkerColor color,

    @Schema(description = "맛집 글 제목 (최대 100자)", example = "맛집 소개", requiredMode = REQUIRED)
    @NotEmpty(message = "제목은 필수입니다.")
    String title,

    @Schema(description = "맛집 글 설명 (최대 1000자)", example = "정말 맛있는 집입니다!", requiredMode = REQUIRED)
    @NotEmpty(message = "설명은 필수입니다.")
    String description,

    @Schema(description = "방문 날짜 및 시간", example = "2025-02-08 12:00:00", requiredMode = REQUIRED)
    @NotNull(message = "날짜는 필수입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
    LocalDateTime date,

    @Schema(description = "맛집 점수 (0 ~ 10)", example = "9")
    @Min(value = 0, message = "점수는 최소 0이어야 합니다.")
    @Max(value = 10, message = "점수는 최대 10이어야 합니다.")
    Integer score
) {}
