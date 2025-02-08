package svsite.matzip.foody.domain.post.api;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import svsite.matzip.foody.domain.auth.ControllerTestSupport;
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.domain.post.api.dto.request.CreatePostDto;
import svsite.matzip.foody.domain.post.api.dto.response.MarkersResponseDto;
import svsite.matzip.foody.domain.post.api.dto.response.PostResponseDto;
import svsite.matzip.foody.domain.post.entity.MarkerColor;

class PostControllerTest extends ControllerTestSupport {

  @DisplayName("사용자가 등록한 모든 맛집 좌표(마커)를 반환한다")
  @Test
  void getAllMarkers() throws Exception {
    // given
    User mockUser = setupAuthenticatedUser();

    List<MarkersResponseDto> markersResponseDtos = getMockedMarkers();

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

  @Test
  @DisplayName("맛집 글을 성공적으로 등록한다")
  void createPost() throws Exception {
    // given
    User mockUser = setupAuthenticatedUser();

    CreatePostDto createPostDto = new CreatePostDto(
        BigDecimal.valueOf(37.5665),
        BigDecimal.valueOf(126.9780),
        MarkerColor.RED,
        "서울특별시 종로구",
        "맛집 소개",
        "정말 맛있는 집입니다!",
        LocalDateTime.of(2025, 2, 8, 12, 0, 0),
        9,
        List.of("https://example.com/image1.jpg", "https://example.com/image2.jpg")
    );

    PostResponseDto responseDto = PostResponseDto.builder()
        .id(1L)
        .latitude(BigDecimal.valueOf(37.5665))
        .longitude(BigDecimal.valueOf(126.9780))
        .color(MarkerColor.RED)
        .address("서울특별시 종로구")
        .title("맛집 소개")
        .description("정말 맛있는 집입니다!")
        .date(LocalDateTime.of(2025, 2, 8, 12, 0, 0))
        .score(9)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    when(postService.createPost(any(CreatePostDto.class), any(User.class))).thenReturn(responseDto);

    // when & then
    mockMvc.perform(post("/posts")
            .header(HttpHeaders.AUTHORIZATION, "Bearer validToken")
            .content(objectMapper.writeValueAsString(createPostDto))  // JSON 변환 후 전송
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.latitude").value(37.5665))
        .andExpect(jsonPath("$.longitude").value(126.9780))
        .andExpect(jsonPath("$.color").value("RED"))
        .andExpect(jsonPath("$.address").value("서울특별시 종로구"))
        .andExpect(jsonPath("$.title").value("맛집 소개"))
        .andExpect(jsonPath("$.description").value("정말 맛있는 집입니다!"))
        .andExpect(jsonPath("$.score").value(9));

    verify(postService).createPost(any(CreatePostDto.class), eq(mockUser));
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

  private User setupAuthenticatedUser() throws Exception {
    User mockUser = User.builder()
        .email("test@example.com")
        .hashedRefreshToken("hashedRefreshToken")
        .build();

    when(authenticatedUserResolver.supportsParameter(any())).thenReturn(true);
    when(authenticatedUserResolver.resolveArgument(any(), any(), any(), any())).thenReturn(mockUser);

    return mockUser;
  }
}
