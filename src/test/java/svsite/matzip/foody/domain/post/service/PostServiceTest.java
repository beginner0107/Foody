package svsite.matzip.foody.domain.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.domain.post.api.dto.request.CreatePostDto;
import svsite.matzip.foody.domain.post.api.dto.request.UpdatePostDto;
import svsite.matzip.foody.domain.post.api.dto.response.MarkersResponseDto;
import svsite.matzip.foody.domain.post.api.dto.response.PostResponseDto;
import svsite.matzip.foody.domain.post.entity.MarkerColor;
import svsite.matzip.foody.domain.post.entity.Post;
import svsite.matzip.foody.domain.post.repository.PostRepository;
import svsite.matzip.foody.domain.post.repository.dto.PostMarkersQueryDto;
import svsite.matzip.foody.global.exception.support.CustomException;

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

  @Test
  @DisplayName("맛집 글을 성공적으로 등록한다")
  void createPost() {
    User mockUser = User.builder().email("test@example.com").nickname("테스터").build();
    CreatePostDto createPostDto = getSampleCreatePostDto();

    when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
      Post post = invocation.getArgument(0);
      ReflectionTestUtils.setField(post, "id", 1L);  // save 후 ID 설정
      return post;
    });

    PostResponseDto responseDto = postService.createPost(createPostDto, mockUser);

    assertNotNull(responseDto, "응답은 null이 아니어야 합니다.");
    assertEquals(1L, responseDto.id(), "ID가 예상 값과 일치해야 합니다.");
    assertEquals(createPostDto.title(), responseDto.title(), "제목이 예상 값과 일치해야 합니다.");

    verify(postRepository).save(any(Post.class));
  }

  @Test
  @DisplayName("맛집 글을 성공적으로 수정한다")
  void updatePost() {
    // given
    User mockUser = User.builder()
        .email("test@example.com")
        .nickname("테스터")
        .build();

    Post existingPost = Post.builder()
        .id(1L)
        .latitude(BigDecimal.valueOf(37.5665))
        .longitude(BigDecimal.valueOf(126.9780))
        .color(MarkerColor.RED)
        .address("서울특별시 종로구")
        .title("기존 맛집 소개")
        .description("기존 설명")
        .date(LocalDateTime.of(2025, 2, 7, 12, 0))
        .score(8)
        .user(mockUser)
        .build();

    UpdatePostDto updatePostDto = new UpdatePostDto(
        MarkerColor.BLUE,
        "수정된 맛집 소개",
        "수정된 설명",
        LocalDateTime.of(2025, 2, 8, 15, 30, 0),
        9
    );

    when(postRepository.findByPostIdAndUser(1L, mockUser)).thenReturn(Optional.of(existingPost));

    // when
    PostResponseDto responseDto = postService.updatePost(1L, updatePostDto, mockUser);

    // then
    assertNotNull(responseDto, "응답은 null이 아니어야 합니다.");
    assertEquals(1L, responseDto.id(), "ID가 예상 값과 일치해야 합니다.");
    assertEquals(updatePostDto.title(), responseDto.title(), "제목이 예상 값과 일치해야 합니다.");
    assertEquals(updatePostDto.color(), responseDto.color(), "마커 색상이 예상 값과 일치해야 합니다.");
    assertEquals(updatePostDto.description(), responseDto.description(), "설명이 예상 값과 일치해야 합니다.");
    assertEquals(updatePostDto.date(), responseDto.date(), "날짜가 예상 값과 일치해야 합니다.");

    verify(postRepository).findByPostIdAndUser(1L, mockUser);
  }

  @Test
  @DisplayName("맛집 글을 성공적으로 삭제한다")
  void deletePost() {
    // given
    User mockUser = User.builder().email("test@example.com").nickname("테스터").build();

    Post existingPost = Post.builder()
        .id(1L)
        .latitude(BigDecimal.valueOf(37.5665))
        .longitude(BigDecimal.valueOf(126.9780))
        .color(MarkerColor.RED)
        .address("서울특별시 종로구")
        .title("기존 맛집 소개")
        .description("기존 설명")
        .date(LocalDateTime.of(2025, 2, 7, 12, 0))
        .score(8)
        .user(mockUser)
        .build();

    // Mock 설정: 게시글이 존재하는 경우
    when(postRepository.findByPostIdAndUser(1L, mockUser)).thenReturn(Optional.of(existingPost));

    // when
    postService.deletePost(1L, mockUser);

    // then
    verify(postRepository).findByPostIdAndUser(1L, mockUser);  // 게시글 조회 검증
    verify(postRepository).delete(existingPost);               // 삭제 메서드 호출 검증
  }

  @Test
  @DisplayName("존재하지 않는 게시글 삭제 시 예외가 발생한다")
  void deletePost_postNotFound_throwsException() {
    // given
    User mockUser = User.builder().email("test@example.com").nickname("테스터").build();

    when(postRepository.findByPostIdAndUser(1L, mockUser)).thenReturn(Optional.empty());

    // when & then
    CustomException exception = assertThrows(CustomException.class, () -> {
      postService.deletePost(1L, mockUser);
    });

    assertEquals("해당 게시물을 찾을 수 없습니다.", exception.getMessage(), "예외 메시지가 예상과 일치해야 합니다.");

    verify(postRepository, never()).delete(any(Post.class));
  }

  @Test
  @DisplayName("등록된 맛집 게시글 목록을 페이지 단위로 성공적으로 조회한다")
  void getPosts_success() {
    // given
    User mockUser = User.builder().email("test@example.com").build();
    PageRequest pageable = PageRequest.of(0, 10);

    List<Post> posts = List.of(
        createMockPost(1L, "맛집 소개 1", "맛있는 집입니다 1"),
        createMockPost(2L, "맛집 소개 2", "맛있는 집입니다 2")
    );

    Page<Post> postPage = new PageImpl<>(posts, pageable, posts.size());

    when(postRepository.findAllRecentPost(pageable, mockUser)).thenReturn(postPage);

    // when
    Page<PostResponseDto> result = postService.getPosts(pageable, mockUser);

    // then
    assertNotNull(result, "결과는 null이 아니어야 합니다.");
    assertEquals(2, result.getContent().size(), "게시글 개수가 예상과 일치해야 합니다.");
    assertEquals("맛집 소개 1", result.getContent().getFirst().title(), "첫 번째 게시글 제목이 예상 값과 일치해야 합니다.");

    verify(postRepository).findAllRecentPost(pageable, mockUser);
  }

  @Test
  @DisplayName("등록된 게시글이 없을 경우 빈 페이지를 반환한다")
  void getPosts_emptyPage() {
    // given
    User mockUser = User.builder().email("empty@example.com").build();
    PageRequest pageable = PageRequest.of(0, 10);

    when(postRepository.findAllRecentPost(pageable, mockUser)).thenReturn(Page.empty(pageable));

    // when
    Page<PostResponseDto> result = postService.getPosts(pageable, mockUser);

    // then
    assertNotNull(result, "결과는 null이 아니어야 합니다.");
    assertTrue(result.isEmpty(), "결과 페이지는 빈 페이지여야 합니다.");
    verify(postRepository).findAllRecentPost(pageable, mockUser);
  }

  @Test
  @DisplayName("게시글 단건을 성공적으로 조회한다")
  void getPostById_success() {
    // given
    User mockUser = User.builder().email("test@example.com").build();

    Post existingPost = Post.builder()
        .id(1L)
        .latitude(BigDecimal.valueOf(37.5665))
        .longitude(BigDecimal.valueOf(126.9780))
        .color(MarkerColor.RED)
        .address("서울특별시 종로구")
        .title("맛집 소개")
        .description("정말 맛있는 집입니다!")
        .date(LocalDateTime.of(2025, 2, 8, 12, 0, 0))
        .score(9)
        .user(mockUser)
        .build();

    when(postRepository.findByPostIdAndUser(1L, mockUser)).thenReturn(Optional.of(existingPost));

    // when
    PostResponseDto responseDto = postService.getPostById(1L, mockUser);

    // then
    assertNotNull(responseDto, "응답은 null이 아니어야 합니다.");
    assertEquals(1L, responseDto.id(), "ID가 예상 값과 일치해야 합니다.");
    assertEquals(existingPost.getTitle(), responseDto.title(), "제목이 예상 값과 일치해야 합니다.");
    assertEquals(existingPost.getLatitude(), responseDto.latitude(), "위도가 예상 값과 일치해야 합니다.");
    assertEquals(existingPost.getLongitude(), responseDto.longitude(), "경도가 예상 값과 일치해야 합니다.");
    assertEquals(existingPost.getColor(), responseDto.color(), "마커 색상이 예상 값과 일치해야 합니다.");

    verify(postRepository).findByPostIdAndUser(1L, mockUser);
  }

  @Test
  @DisplayName("존재하지 않는 게시글 조회 시 예외가 발생한다")
  void getPostById_postNotFound_throwsException() {
    // given
    User mockUser = User.builder().email("test@example.com").build();

    when(postRepository.findByPostIdAndUser(1L, mockUser)).thenReturn(Optional.empty());

    // when & then
    CustomException exception = assertThrows(CustomException.class,
        () -> postService.getPostById(1L, mockUser));

    assertEquals("해당 게시물을 찾을 수 없습니다.", exception.getMessage(), "예외 메시지가 예상과 일치해야 합니다.");

    verify(postRepository).findByPostIdAndUser(1L, mockUser);
  }

  @Test
  @DisplayName("특정 년도와 월에 해당하는 게시글 목록을 조회한다")
  void getPostsByMonth_success() {
    // given
    User mockUser = User.builder().email("test@example.com").build();
    int year = 2025;
    int month = 2;

    List<Post> posts = List.of(
        createMockPost(1L, "맛집 소개 1", "맛있는 집입니다 1", LocalDateTime.of(2025, 2, 8, 12, 0, 0)),
        createMockPost(2L, "맛집 소개 2", "맛있는 집입니다 2", LocalDateTime.of(2025, 2, 9, 14, 30, 0)),
        createMockPost(3L, "맛집 소개 3", "맛있는 집입니다 3", LocalDateTime.of(2025, 2, 8, 18, 0, 0))
    );

    when(postRepository.findPostsByMonth(year, month, mockUser)).thenReturn(posts);

    // when
    Map<Integer, List<PostResponseDto>> result = postService.getPostsByMonth(year, month, mockUser);

    // then
    assertNotNull(result, "결과는 null이 아니어야 합니다.");
    assertEquals(2, result.size(), "두 개의 날짜 그룹이 있어야 합니다.");
    assertTrue(result.containsKey(8), "8일 날짜 그룹이 존재해야 합니다.");
    assertTrue(result.containsKey(9), "9일 날짜 그룹이 존재해야 합니다.");

    List<PostResponseDto> day8Posts = result.get(8);
    assertEquals(2, day8Posts.size(), "8일 날짜 그룹에 두 개의 게시글이 있어야 합니다.");
    assertEquals("맛집 소개 1", day8Posts.get(0).title(), "첫 번째 게시글 제목이 예상과 일치해야 합니다.");
    assertEquals("맛집 소개 3", day8Posts.get(1).title(), "두 번째 게시글 제목이 예상과 일치해야 합니다.");

    List<PostResponseDto> day9Posts = result.get(9);
    assertEquals(1, day9Posts.size(), "9일 날짜 그룹에 하나의 게시글이 있어야 합니다.");
    assertEquals("맛집 소개 2", day9Posts.getFirst().title(), "9일 날짜 그룹의 게시글 제목이 예상과 일치해야 합니다.");

    verify(postRepository).findPostsByMonth(year, month, mockUser);
  }

  @Test
  @DisplayName("특정 년도와 월에 게시글이 없을 경우 빈 맵을 반환한다")
  void getPostsByMonth_emptyResult() {
    // given
    User mockUser = User.builder().email("empty@example.com").build();
    int year = 2025;
    int month = 1;

    when(postRepository.findPostsByMonth(year, month, mockUser)).thenReturn(
        Collections.emptyList());

    // when
    Map<Integer, List<PostResponseDto>> result = postService.getPostsByMonth(year, month, mockUser);

    // then
    assertNotNull(result, "결과는 null이 아니어야 합니다.");
    assertTrue(result.isEmpty(), "결과는 빈 맵이어야 합니다.");
    verify(postRepository).findPostsByMonth(year, month, mockUser);
  }

  @Test
  @DisplayName("사용자가 등록한 게시글을 제목 또는 주소로 성공적으로 검색한다")
  void searchMyPostsByTitleAndAddress_success() {
    // given
    User mockUser = User.builder()
        .email("test@example.com")
        .nickname("테스터")
        .build();

    Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "date"));

    List<Post> posts = List.of(
        createMockPost(1L, "맛집 소개 1", "서울특별시 종로구"),
        createMockPost(2L, "맛집 소개 2", "부산광역시 중구")
    );

    Page<Post> postPage = new PageImpl<>(posts, pageable, posts.size());

    when(postRepository.searchByTitleOrAddress(eq("맛집"), eq(mockUser), eq(pageable)))
        .thenReturn(postPage);

    // when
    Page<PostResponseDto> result = postService.searchMyPostsByTitleAndAddress(pageable, "맛집",
        mockUser);

    // then
    assertNotNull(result, "결과는 null이 아니어야 합니다.");
    assertEquals(2, result.getContent().size(), "게시글 개수가 예상과 일치해야 합니다.");
    assertEquals("맛집 소개 1", result.getContent().getFirst().title(), "첫 번째 게시글 제목이 예상 값과 일치해야 합니다.");
    assertEquals("서울특별시 종로구", result.getContent().getFirst().address(),
        "첫 번째 게시글 주소가 예상 값과 일치해야 합니다.");

    verify(postRepository).searchByTitleOrAddress(eq("맛집"), eq(mockUser), eq(pageable));
  }

  @Test
  @DisplayName("검색 결과가 없을 경우 빈 페이지를 반환한다")
  void searchMyPostsByTitleAndAddress_noResults() {
    // given
    User mockUser = User.builder().email("test@example.com").nickname("테스터").build();
    Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "date"));

    when(postRepository.searchByTitleOrAddress(eq("없는 키워드"), eq(mockUser), eq(pageable)))
        .thenReturn(Page.empty(pageable));

    // when
    Page<PostResponseDto> result = postService.searchMyPostsByTitleAndAddress(pageable, "없는 키워드",
        mockUser);

    // then
    assertNotNull(result, "결과는 null이 아니어야 합니다.");
    assertTrue(result.isEmpty(), "결과 페이지는 빈 페이지여야 합니다.");
    verify(postRepository).searchByTitleOrAddress(eq("없는 키워드"), eq(mockUser), eq(pageable));
  }

  private Post createMockPost(Long id, String title, String description) {
    return Post.builder()
        .id(id)
        .latitude(BigDecimal.valueOf(37.5665))
        .longitude(BigDecimal.valueOf(126.9780))
        .color(MarkerColor.RED)
        .address("서울특별시 종로구")
        .title(title)
        .description(description)
        .date(LocalDateTime.now())
        .score(9)
        .user(User.builder().email("test@example.com").build())
        .build();
  }

  private Post createMockPost(Long id, String title, String description, LocalDateTime date) {
    return Post.builder()
        .id(id)
        .latitude(BigDecimal.valueOf(37.5665))
        .longitude(BigDecimal.valueOf(126.9780))
        .color(MarkerColor.RED)
        .address("서울특별시 종로구")
        .title(title)
        .description(description)
        .date(date)
        .score(9)
        .user(User.builder().email("test@example.com").build())
        .build();
  }

  private CreatePostDto getSampleCreatePostDto() {
    return new CreatePostDto(
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
  }


  private PostMarkersQueryDto createMarker(Long id, double lat, double lon, MarkerColor color,
      int score) {
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
