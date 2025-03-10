package svsite.matzip.foody.domain.post.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
    LocalDateTime updatedAt,
    @Schema(description = "게시글에 첨부된 이미지 목록")
    List<ImageResponseDto> images,
    @Schema(description = "즐겨찾기 유무")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Boolean isFavorite
) {

  public static PostResponseDto from(Post post) {
    return from(post, null);
  }

  public static PostResponseDto fromWithFavorite(Post post, boolean isFavorite) {
    return from(post, isFavorite);
  }

  public static PostResponseDto from(Post post, Boolean isFavorite) {
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
        .images(post.getImages().stream()
            .sorted(
                Comparator.comparingLong(image -> Optional.ofNullable(image.getId()).orElse(0L)))
            .map(image -> ImageResponseDto.builder()
                .id(image.getId())
                .uri(image.getUri())
                .createdAt(image.getCreatedAt())
                .updatedAt(image.getUpdatedAt())
                .deletedAt(image.getDeletedAt())
                .build())
            .toList())
        .createdAt(post.getCreatedAt())
        .updatedAt(post.getUpdatedAt())
        .isFavorite(isFavorite)
        .build();
  }
}
