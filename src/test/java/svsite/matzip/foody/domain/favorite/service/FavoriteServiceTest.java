package svsite.matzip.foody.domain.favorite.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static svsite.matzip.foody.global.exception.errorCode.ErrorCodes.POST_NOT_FOUND;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.domain.favorite.api.response.ToggleFavoriteResponseDto;
import svsite.matzip.foody.domain.favorite.entity.Favorite;
import svsite.matzip.foody.domain.favorite.repository.FavoriteRepository;
import svsite.matzip.foody.domain.post.api.dto.response.PostResponseDto;
import svsite.matzip.foody.domain.post.entity.MarkerColor;
import svsite.matzip.foody.domain.post.entity.Post;
import svsite.matzip.foody.domain.post.repository.PostRepository;
import svsite.matzip.foody.global.exception.support.CustomException;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

  @InjectMocks
  private FavoriteService favoriteService;

  @Mock
  private FavoriteRepository favoriteRepository;

  @Mock
  private PostRepository postRepository;

  @Test
  @DisplayName("게시글 즐겨찾기 토글 - 즐겨찾기 추가")
  void toggleFavorite_add() {
    // given
    long postId = 1L;
    User mockUser = User.builder().email("test@example.com").build();
    Post mockPost = createMockPost(postId);

    when(postRepository.findById(postId)).thenReturn(Optional.of(mockPost));
    when(favoriteRepository.findByPostAndUser(mockPost, mockUser)).thenReturn(Optional.empty());

    // when
    ToggleFavoriteResponseDto response = favoriteService.toggleFavorite(postId, mockUser);

    // then
    assertNotNull(response, "응답은 null이 아니어야 합니다.");
    assertTrue(response.isFavorite(), "즐겨찾기가 추가되어야 합니다.");
    assertEquals(postId, response.postId(), "반환된 게시글 ID가 일치해야 합니다.");

    verify(favoriteRepository).save(any(Favorite.class));
  }

  @Test
  @DisplayName("게시글 즐겨찾기 토글 - 즐겨찾기 삭제")
  void toggleFavorite_remove() {
    // given
    long postId = 1L;
    User mockUser = User.builder().email("test@example.com").build();
    Post mockPost = createMockPost(postId);
    Favorite existingFavorite = Favorite.builder().post(mockPost).user(mockUser).build();

    when(postRepository.findById(postId)).thenReturn(Optional.of(mockPost));
    when(favoriteRepository.findByPostAndUser(mockPost, mockUser)).thenReturn(Optional.of(existingFavorite));

    // when
    ToggleFavoriteResponseDto response = favoriteService.toggleFavorite(postId, mockUser);

    // then
    assertNotNull(response, "응답은 null이 아니어야 합니다.");
    assertFalse(response.isFavorite(), "즐겨찾기가 제거되어야 합니다.");
    assertEquals(postId, response.postId(), "반환된 게시글 ID가 일치해야 합니다.");

    verify(favoriteRepository).delete(existingFavorite);
  }

  @Test
  @DisplayName("존재하지 않는 게시글에 대해 즐겨찾기 토글 시 예외 발생")
  void toggleFavorite_postNotFound_throwsException() {
    // given
    long postId = 1L;
    User mockUser = User.builder().email("test@example.com").build();

    when(postRepository.findById(postId)).thenReturn(Optional.empty());

    // when & then
    CustomException exception = assertThrows(CustomException.class, () -> {
      favoriteService.toggleFavorite(postId, mockUser);
    });

    assertEquals(POST_NOT_FOUND.defaultMessage(), exception.getMessage(), "예외 메시지가 예상과 일치해야 합니다.");
    verify(favoriteRepository, never()).save(any(Favorite.class));
  }

  @Test
  @DisplayName("사용자가 즐겨찾기한 게시글 목록을 페이지 단위로 조회한다")
  void getMyFavoritePosts() {
    // given
    User mockUser = User.builder().email("test@example.com").build();
    PageRequest pageable = PageRequest.of(0, 10);

    List<Favorite> favorites = List.of(
        Favorite.builder().post(createMockPost(1L)).user(mockUser).build(),
        Favorite.builder().post(createMockPost(2L)).user(mockUser).build()
    );

    Page<Favorite> favoritePage = new PageImpl<>(favorites, pageable, favorites.size());

    when(favoriteRepository.findFavoritesByUser(mockUser, pageable)).thenReturn(favoritePage);

    // when
    Page<PostResponseDto> responsePage = favoriteService.getMyFavoritePosts(pageable, mockUser);

    // then
    assertNotNull(responsePage, "결과 페이지는 null이 아니어야 합니다.");
    assertEquals(2, responsePage.getContent().size(), "게시글 개수가 예상과 일치해야 합니다.");
    assertEquals(1L, responsePage.getContent().get(0).id(), "첫 번째 게시글 ID가 예상 값과 일치해야 합니다.");

    verify(favoriteRepository).findFavoritesByUser(mockUser, pageable);
  }

  private Post createMockPost(Long id) {
    return Post.builder()
        .id(id)
        .latitude(BigDecimal.valueOf(37.5665))
        .longitude(BigDecimal.valueOf(126.9780))
        .color(MarkerColor.RED)
        .address("서울특별시 종로구")
        .title("맛집 소개")
        .description("맛집 설명")
        .date(LocalDateTime.now())
        .score(9)
        .user(User.builder().email("test@example.com").build())
        .build();
  }
}
