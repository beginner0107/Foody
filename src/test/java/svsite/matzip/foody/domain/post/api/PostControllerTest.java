package svsite.matzip.foody.domain.post.api;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static svsite.matzip.foody.global.exception.errorCode.ErrorCodes.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import svsite.matzip.foody.domain.auth.ControllerTestSupport;
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.domain.post.api.dto.request.CreatePostDto;
import svsite.matzip.foody.domain.post.api.dto.request.UpdatePostDto;
import svsite.matzip.foody.domain.post.api.dto.response.MarkersResponseDto;
import svsite.matzip.foody.domain.post.api.dto.response.PostResponseDto;
import svsite.matzip.foody.domain.post.entity.MarkerColor;
import svsite.matzip.foody.domain.post.entity.Post;
import svsite.matzip.foody.global.exception.errorCode.ErrorCodes;
import svsite.matzip.foody.global.exception.support.CustomException;

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

  @Test
  @DisplayName("맛집 글을 성공적으로 수정한다")
  void updatePost() throws Exception {
    // given
    User mockUser = setupAuthenticatedUser();

    UpdatePostDto updatePostDto = new UpdatePostDto(
        MarkerColor.BLUE,
        "맛집 수정 소개",
        "수정된 정말 맛있는 집입니다!",
        LocalDateTime.of(2025, 2, 8, 18, 0, 0),
        8
    );

    PostResponseDto responseDto = PostResponseDto.builder()
        .id(1L)
        .latitude(BigDecimal.valueOf(37.5665))
        .longitude(BigDecimal.valueOf(126.9780))
        .color(MarkerColor.BLUE)
        .address("서울특별시 종로구")
        .title("맛집 수정 소개")
        .description("수정된 정말 맛있는 집입니다!")
        .date(LocalDateTime.of(2025, 2, 8, 18, 0, 0))
        .score(8)
        .createdAt(LocalDateTime.now().minusDays(1))
        .updatedAt(LocalDateTime.now())
        .build();

    when(postService.updatePost(eq(1L), any(UpdatePostDto.class), any(User.class))).thenReturn(responseDto);

    // when & then
    mockMvc.perform(patch("/posts/{id}", 1L)
            .header(HttpHeaders.AUTHORIZATION, "Bearer validToken")
            .content(objectMapper.writeValueAsString(updatePostDto))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.latitude").value(37.5665))
        .andExpect(jsonPath("$.longitude").value(126.9780))
        .andExpect(jsonPath("$.color").value("BLUE"))
        .andExpect(jsonPath("$.address").value("서울특별시 종로구"))
        .andExpect(jsonPath("$.title").value("맛집 수정 소개"))
        .andExpect(jsonPath("$.description").value("수정된 정말 맛있는 집입니다!"))
        .andExpect(jsonPath("$.score").value(8));

    verify(postService).updatePost(eq(1L), any(UpdatePostDto.class), eq(mockUser));
  }

  @Test
  @DisplayName("사용자가 등록한 모든 맛집 게시글 목록을 페이지 단위로 조회한다")
  void getPosts() throws Exception {
    // given
    User mockUser = setupAuthenticatedUser();

    List<PostResponseDto> postList = List.of(
        createPostResponseDto(1L, 37.5665, 126.9780, MarkerColor.RED, "서울특별시 종로구", "맛집 소개 1"),
        createPostResponseDto(2L, 35.1796, 129.0756, MarkerColor.BLUE, "부산광역시 중구", "맛집 소개 2")
    );

    Page<PostResponseDto> responsePage = new PageImpl<>(postList);

    when(postService.getPosts(any(PageRequest.class), any(User.class))).thenReturn(responsePage);

    // when & then
    mockMvc.perform(get("/posts/my")
            .header(HttpHeaders.AUTHORIZATION, "Bearer validToken")
            .param("page", "0")
            .param("size", "10")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].id").value(1))
        .andExpect(jsonPath("$.content[0].latitude").value(37.5665))
        .andExpect(jsonPath("$.content[0].longitude").value(126.9780))
        .andExpect(jsonPath("$.content[0].color").value("RED"))
        .andExpect(jsonPath("$.content[0].address").value("서울특별시 종로구"))
        .andExpect(jsonPath("$.content[0].title").value("맛집 소개 1"))
        .andExpect(jsonPath("$.content[1].title").value("맛집 소개 2"))
        .andDo(print());

    verify(postService).getPosts(any(PageRequest.class), eq(mockUser));
  }

  @Test
  @DisplayName("게시글 단건을 성공적으로 조회한다")
  void getPostById() throws Exception {
    // given
    User mockUser = setupAuthenticatedUser();

    PostResponseDto postResponseDto = PostResponseDto.builder()
        .id(1L)
        .latitude(BigDecimal.valueOf(37.5665))
        .longitude(BigDecimal.valueOf(126.9780))
        .color(MarkerColor.RED)
        .address("서울특별시 종로구")
        .title("맛집 소개")
        .description("정말 맛있는 집입니다!")
        .date(LocalDateTime.of(2025, 2, 8, 12, 0, 0))
        .score(9)
        .createdAt(LocalDateTime.now().minusDays(1))
        .updatedAt(LocalDateTime.now())
        .build();

    when(postService.getPostById(eq(1L), any(User.class))).thenReturn(postResponseDto);

    // when & then
    mockMvc.perform(get("/posts/{id}", 1L)
            .header(HttpHeaders.AUTHORIZATION, "Bearer validToken")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.latitude").value(37.5665))
        .andExpect(jsonPath("$.longitude").value(126.9780))
        .andExpect(jsonPath("$.color").value("RED"))
        .andExpect(jsonPath("$.address").value("서울특별시 종로구"))
        .andExpect(jsonPath("$.title").value("맛집 소개"))
        .andExpect(jsonPath("$.description").value("정말 맛있는 집입니다!"))
        .andExpect(jsonPath("$.score").value(9))
        .andDo(print());

    verify(postService).getPostById(eq(1L), eq(mockUser));
  }

  @Test
  @DisplayName("존재하지 않는 게시글 조회 시 404 상태코드를 반환한다")
  void getPostById_postNotFound() throws Exception {
    // given
    User mockUser = setupAuthenticatedUser();

    when(postService.getPostById(eq(1L), any(User.class))).thenThrow(new CustomException(POST_NOT_FOUND));

    // when & then
    mockMvc.perform(get("/posts/{id}", 1L)
            .header(HttpHeaders.AUTHORIZATION, "Bearer validToken")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(POST_NOT_FOUND.defaultMessage()))
        .andDo(print());

    verify(postService).getPostById(eq(1L), eq(mockUser));
  }

  @Test
  @DisplayName("특정 년도와 월의 게시글 목록을 날짜별로 조회한다")
  void getPostsByMonth_success() throws Exception {
    // given
    User mockUser = setupAuthenticatedUser();
    int year = 2025;
    int month = 2;

    Map<Integer, List<PostResponseDto>> responseMap = Map.of(
        8, List.of(
            createPostResponseDto(1L, "맛집 소개 1", LocalDateTime.of(2025, 2, 8, 12, 0, 0)),
            createPostResponseDto(2L, "맛집 소개 2", LocalDateTime.of(2025, 2, 8, 18, 0, 0))
        ),
        9, List.of(
            createPostResponseDto(3L, "맛집 소개 3", LocalDateTime.of(2025, 2, 9, 14, 0, 0))
        )
    );

    when(postService.getPostsByMonth(year, month, mockUser)).thenReturn(responseMap);

    // when & then
    mockMvc.perform(get("/posts")
            .header(HttpHeaders.AUTHORIZATION, "Bearer validToken")
            .param("year", String.valueOf(year))
            .param("month", String.valueOf(month))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$['8'].length()").value(2))
        .andExpect(jsonPath("$['8'][0].id").value(1))
        .andExpect(jsonPath("$['8'][0].title").value("맛집 소개 1"))
        .andExpect(jsonPath("$['8'][1].id").value(2))
        .andExpect(jsonPath("$['9'].length()").value(1))
        .andExpect(jsonPath("$['9'][0].title").value("맛집 소개 3"));


    verify(postService).getPostsByMonth(year, month, mockUser);
  }

  @Test
  @DisplayName("게시글이 없을 경우 빈 맵을 반환한다")
  void getPostsByMonth_emptyResult() throws Exception {
    // given
    User mockUser = setupAuthenticatedUser();
    int year = 2025;
    int month = 1;

    when(postService.getPostsByMonth(year, month, mockUser)).thenReturn(Collections.emptyMap());

    // when & then
    mockMvc.perform(get("/posts")
            .header(HttpHeaders.AUTHORIZATION, "Bearer validToken")
            .param("year", String.valueOf(year))
            .param("month", String.valueOf(month))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0))  // 빈 결과 확인
        .andDo(print());

    verify(postService).getPostsByMonth(year, month, mockUser);
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

  private PostResponseDto createPostResponseDto(Long id, double lat, double lon, MarkerColor color, String address, String title) {
    return PostResponseDto.builder()
        .id(id)
        .latitude(BigDecimal.valueOf(lat))
        .longitude(BigDecimal.valueOf(lon))
        .color(color)
        .address(address)
        .title(title)
        .description("맛집 설명입니다.")
        .date(LocalDateTime.of(2025, 2, 8, 12, 0, 0))
        .score(9)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }

  private PostResponseDto createPostResponseDto(Long id, String title, LocalDateTime date) {
    return PostResponseDto.builder()
        .id(id)
        .latitude(BigDecimal.valueOf(37.5665))
        .longitude(BigDecimal.valueOf(126.9780))
        .color(MarkerColor.RED)
        .address("서울특별시 종로구")
        .title(title)
        .description("맛집 설명입니다.")
        .date(date)
        .score(9)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }
}
