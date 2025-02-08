package svsite.matzip.foody.domain.post.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Builder;
import svsite.matzip.foody.domain.post.entity.MarkerColor;
import svsite.matzip.foody.domain.post.repository.dto.PostMarkersQueryDto;

@Schema(description = "맛집 마커 정보를 나타내는 응답 DTO")
@Builder
public record MarkersResponseDto(
    @Schema(description = "마커 ID", example = "1")
    Long id,

    @Schema(description = "마커 위도 좌표", example = "37.566500")
    BigDecimal latitude,

    @Schema(description = "마커 경도 좌표", example = "126.978000")
    BigDecimal longitude,

    @Schema(description = "마커 색상", example = "RED")
    MarkerColor color,

    @Schema(description = "마커 점수", example = "10")
    Integer score
) {

  public static MarkersResponseDto from(PostMarkersQueryDto dto) {
    return MarkersResponseDto.builder()
        .id(dto.getId())
        .latitude(dto.getLatitude())
        .longitude(dto.getLongitude())
        .color(dto.getColor())
        .score(dto.getScore())
        .build();
  }
}
