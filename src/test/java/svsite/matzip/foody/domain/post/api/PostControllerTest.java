package svsite.matzip.foody.domain.post.api;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import svsite.matzip.foody.domain.auth.ControllerTestSupport;
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.domain.post.api.dto.response.MarkersResponseDto;
import svsite.matzip.foody.domain.post.entity.MarkerColor;

class PostControllerTest extends ControllerTestSupport {

  @DisplayName("사용자가 등록한 모든 맛집 좌표(마커)를 반환한다")
  @Test
  void getAllMarkers() throws Exception {
    // given
    User mockUser = User.builder()
        .email("test@example.com")
        .hashedRefreshToken("hashedRefreshToken")
        .build();

    List<MarkersResponseDto> markersResponseDtos = getMockedMarkers();

    when(authenticatedUserResolver.supportsParameter(any())).thenReturn(true);
    when(authenticatedUserResolver.resolveArgument(any(), any(), any(), any())).thenReturn(mockUser);
    when(postService.getAllMarkers(any(User.class))).thenReturn(markersResponseDtos);

    // when & then
    mockMvc.perform(get("/markers/my")
            .header(HttpHeaders.AUTHORIZATION, "Bearer validRefreshToken")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(3))  // 응답 크기 확인
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].latitude").value(37.5665))
        .andExpect(jsonPath("$[0].longitude").value(126.9780))
        .andExpect(jsonPath("$[0].color").value("RED"))
        .andExpect(jsonPath("$[0].score").value(10))
        .andExpect(jsonPath("$[1].color").value("BLUE"))
        .andExpect(jsonPath("$[2].color").value("GREEN"));
  }

  private MarkersResponseDto createMarker(Long id, double latitude, double longitude, MarkerColor color, int score) {
    return MarkersResponseDto.builder()
        .id(id)
        .latitude(BigDecimal.valueOf(latitude))
        .longitude(BigDecimal.valueOf(longitude))
        .color(color)
        .score(score)
        .build();
  }

  private List<MarkersResponseDto> getMockedMarkers() {
    return Arrays.asList(
        createMarker(1L, 37.5665, 126.9780, MarkerColor.RED, 10),
        createMarker(2L, 35.1796, 129.0756, MarkerColor.BLUE, 8),
        createMarker(3L, 33.4996, 126.5312, MarkerColor.GREEN, 9)
    );
  }
}
