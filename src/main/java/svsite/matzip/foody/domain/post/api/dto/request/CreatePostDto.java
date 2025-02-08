package svsite.matzip.foody.domain.post.api.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import svsite.matzip.foody.domain.post.entity.MarkerColor;


@Schema(description = "게시글 생성 요청 DTO")
public record CreatePostDto(

    @Schema(description = "위도 값 (최대 소수점 6자리, 범위: -90 ~ 90)", example = "37.566500")
    @NotNull(message = "위도 값은 필수입니다.")
    @DecimalMin(value = "-90.000000", inclusive = true, message = "위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.000000", inclusive = true, message = "위도는 90 이하이어야 합니다.")
    BigDecimal latitude,

    @Schema(description = "경도 값 (최대 소수점 6자리, 범위: -180 ~ 180)", example = "126.978000")
    @NotNull(message = "경도 값은 필수입니다.")
    @DecimalMin(value = "-180.000000", inclusive = true, message = "경도는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.000000", inclusive = true, message = "경도는 180 이하이어야 합니다.")
    BigDecimal longitude,

    @Schema(description = "마커 색상", example = "RED")
    @NotNull(message = "마커 색상은 필수입니다.")
    MarkerColor color,

    @Schema(description = "주소 (최대 255자)", example = "서울특별시 강남구 테헤란로 123")
    @Size(max = 255, message = "주소는 최대 255자까지 입력 가능합니다.")
    String address,

    @Schema(description = "제목 (최대 100자)", example = "맛집 추천")
    @NotEmpty(message = "제목은 필수입니다.")
    @Size(max = 100, message = "제목은 최대 100자까지 입력 가능합니다.")
    String title,

    @Schema(description = "설명 (최소 10자, 최대 1000자)", example = "여기는 정말 맛있는 음식점입니다.")
    @NotEmpty(message = "설명은 필수입니다.")
    @Size(max = 1000, message = "설명은 최소 10자, 최대 1000자까지 입력 가능합니다.")
    String description,

    @Schema(description = "날짜 및 시간 (형식: yyyy-MM-dd HH:mm:ss)", example = "2025-02-07 18:30:00")
    @NotNull(message = "날짜는 필수입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
    LocalDateTime date,

    @Schema(description = "점수 (0 ~ 10)", example = "8")
    @Min(value = 0, message = "점수는 최소 0이어야 합니다.")
    @Max(value = 10, message = "점수는 최대 10이어야 합니다.")
    Integer score,

    @Schema(description = "이미지 URI 목록 (각 URI는 최대 255자)", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
    List<@Size(max = 255, message = "이미지 URI는 최대 255자까지 입력 가능합니다.") String> imageUris
) {}
