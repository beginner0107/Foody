package svsite.matzip.foody.domain.post.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import svsite.matzip.foody.domain.post.entity.MarkerColor;
import svsite.matzip.foody.domain.post.entity.Post;

@Builder
public record PostResponseDto(
    @Schema(description = "게시글 ID", example = "1")
    Long id,
    @Schema(description = "위도 값 (최대 소수점 6자리)", example = "37.566500")
    BigDecimal latitude,
    @Schema(description = "경도 값 (최대 소수점 6자리)", example = "126.978000")
    BigDecimal longitude,
    @Schema(description = "마커 색상", example = "RED")
    MarkerColor color,
    @Schema(description = "주소", example = "서울특별시 종로구")
    String address,
    @Schema(description = "게시글 제목", example = "맛집 소개")
    String title,
    @Schema(description = "게시글 설명", example = "정말 맛있는 집입니다!")
    String description,
    @Schema(description = "방문 날짜", example = "2025-02-08T12:00:00")
    LocalDateTime date,
    @Schema(description = "맛집 평점 (0~10)", example = "9")
    Integer score,
    @Schema(description = "생성 날짜 및 시간", example = "2025-02-08T12:00:00")
    LocalDateTime createdAt,
    @Schema(description = "수정 날짜 및 시간", example = "2025-02-08T13:00:00")
    LocalDateTime updatedAt
) {

  public static PostResponseDto from(Post post) {
    return PostResponseDto.builder()
        .id(post.getId())
        .latitude(post.getLatitude())
        .longitude(post.getLongitude())
        .color(post.getColor())
        .address(post.getAddress())
        .title(post.getTitle())
        .description(post.getDescription())
        .date(post.getDate())
        .score(post.getScore())
        .createdAt(post.getCreatedAt())
        .updatedAt(post.getUpdatedAt())
        .build();
  }
}
