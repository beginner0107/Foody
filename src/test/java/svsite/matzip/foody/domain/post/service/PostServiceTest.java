package svsite.matzip.foody.domain.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.domain.post.api.dto.response.MarkersResponseDto;
import svsite.matzip.foody.domain.post.entity.MarkerColor;
import svsite.matzip.foody.domain.post.repository.PostRepository;
import svsite.matzip.foody.domain.post.repository.dto.PostMarkersQueryDto;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
  @InjectMocks
  private PostService postService;

  @Mock
  private PostRepository postRepository;

  @Test
  @DisplayName("등록된 맛집 마커가 없을 경우 빈 리스트를 반환한다")
  void getAllMarkers_emptyList() {
    // given
    User mockUser = User.builder().email("empty@example.com").build();

    when(postRepository.getAllMarkers(mockUser)).thenReturn(Collections.emptyList());

    // when
    List<MarkersResponseDto> allMarkers = postService.getAllMarkers(mockUser);

    // then
    assertNotNull(allMarkers, "결과 리스트는 null이 아니어야 합니다.");
    assertTrue(allMarkers.isEmpty(), "결과 리스트는 빈 리스트여야 합니다.");
  }

  private BigDecimal roundCoordinate(double value) {
    return BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP);
  }

  @Test
  @DisplayName("등록된 맛집 마커 목록을 올바르게 조회한다")
  void getAllMarkers() {
    // given
    User mockUser = User.builder().email("test@example.com").build();
    List<PostMarkersQueryDto> markersResponseDtos = getMockedMarkers();

    // Mock 설정
    when(postRepository.getAllMarkers(any(User.class))).thenReturn(markersResponseDtos);

    // when
    List<MarkersResponseDto> allMarkers = postService.getAllMarkers(mockUser);

    // then
    assertThat(allMarkers).hasSize(3)
        .extracting("id", "latitude", "longitude", "color", "score")
        .containsExactlyInAnyOrder(
            tuple(1L, roundCoordinate(37.5665), roundCoordinate(126.9780), MarkerColor.RED, 10),
            tuple(2L, roundCoordinate(35.1796), roundCoordinate(129.0756), MarkerColor.BLUE, 8),
            tuple(3L, roundCoordinate(33.4996), roundCoordinate(126.5312), MarkerColor.GREEN, 9)
        );
  }

  private PostMarkersQueryDto createMarker(Long id, double lat, double lon, MarkerColor color, int score) {
    return PostMarkersQueryDto.builder()
        .id(id)
        .latitude(BigDecimal.valueOf(lat).setScale(6, RoundingMode.HALF_UP))
        .longitude(BigDecimal.valueOf(lon).setScale(6, RoundingMode.HALF_UP))
        .color(color)
        .score(score)
        .build();
  }

  private List<PostMarkersQueryDto> getMockedMarkers() {
    return Arrays.asList(
        createMarker(1L, 37.5665, 126.9780, MarkerColor.RED, 10),
        createMarker(2L, 35.1796, 129.0756, MarkerColor.BLUE, 8),
        createMarker(3L, 33.4996, 126.5312, MarkerColor.GREEN, 9)
    );
  }
}
