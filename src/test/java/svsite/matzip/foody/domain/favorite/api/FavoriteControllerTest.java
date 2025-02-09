package svsite.matzip.foody.domain.favorite.api;

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
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import svsite.matzip.foody.domain.auth.ControllerTestSupport;
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.domain.favorite.api.response.ToggleFavoriteResponseDto;
import svsite.matzip.foody.domain.post.api.dto.response.PostResponseDto;
import svsite.matzip.foody.domain.post.entity.MarkerColor;

class FavoriteControllerTest extends ControllerTestSupport {

  private final String AUTH_HEADER = "Bearer validToken";

  @Test
  @DisplayName("게시글 즐겨찾기를 성공적으로 토글한다")
  void toggleFavorite_success() throws Exception {
    // given
    long postId = 1L;
    ToggleFavoriteResponseDto responseDto = new ToggleFavoriteResponseDto(postId, true);

    when(favoriteService.toggleFavorite(eq(postId), any(User.class))).thenReturn(responseDto);

    // when & then
    mockMvc.perform(post("/favorites/{id}", postId)
            .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.postId").value(postId))
        .andExpect(jsonPath("$.isFavorite").value(true))
        .andDo(print());

    verify(favoriteService).toggleFavorite(eq(postId), any(User.class));
  }

  @Test
  @DisplayName("내 즐겨찾기 게시글 목록을 성공적으로 조회한다")
  void getMyFavoritePosts_success() throws Exception {
    // given
    List<PostResponseDto> favoritePosts = List.of(
        new PostResponseDto(1L, BigDecimal.valueOf(37.5665), BigDecimal.valueOf(126.9780),
            MarkerColor.RED, "서울특별시 종로구", "맛집 소개 1", "맛집 설명", LocalDateTime.now(),
            9, LocalDateTime.now(), LocalDateTime.now(), List.of(), true),
        new PostResponseDto(2L, BigDecimal.valueOf(35.1796), BigDecimal.valueOf(129.0756),
            MarkerColor.BLUE, "부산광역시 중구", "맛집 소개 2", "맛집 설명", LocalDateTime.now(),
            8, LocalDateTime.now(), LocalDateTime.now(), List.of(), true)
    );
    Page<PostResponseDto> responsePage = new PageImpl<>(favoritePosts);

    when(favoriteService.getMyFavoritePosts(any(PageRequest.class), any(User.class))).thenReturn(responsePage);

    // when & then
    mockMvc.perform(get("/favorites/my")
            .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
            .param("page", "0")
            .param("size", "10")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].id").value(1L))
        .andExpect(jsonPath("$.content[0].title").value("맛집 소개 1"))
        .andExpect(jsonPath("$.content[1].title").value("맛집 소개 2"))
        .andDo(print());

    verify(favoriteService).getMyFavoritePosts(any(PageRequest.class), any(User.class));
  }
}
