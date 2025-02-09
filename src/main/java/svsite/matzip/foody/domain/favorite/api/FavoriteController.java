package svsite.matzip.foody.domain.favorite.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.domain.favorite.api.response.ToggleFavoriteResponseDto;
import svsite.matzip.foody.domain.favorite.service.FavoriteService;
import svsite.matzip.foody.domain.post.api.dto.response.PostResponseDto;
import svsite.matzip.foody.global.auth.AuthenticatedUser;

@Tag(name = "Favorite", description = "게시글 즐겨찾기 API")
@RequestMapping("/favorites")
@RestController
@RequiredArgsConstructor
public class FavoriteController {

  private final FavoriteService favoriteService;

  @Operation(
      summary = "게시글 즐겨찾기 토글",
      description = "특정 게시글의 즐겨찾기 상태를 추가 또는 제거합니다.",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "즐겨찾기 토글 성공 (변경된 게시글 ID 반환)"),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청")
  })
  @PostMapping("/{id}")
  public ResponseEntity<ToggleFavoriteResponseDto> toggleFavorite(
      @Parameter(description = "즐겨찾기할 게시글 ID", example = "1", required = true)
      @PathVariable long id,
      @AuthenticatedUser User user
  ) {
    return ResponseEntity.ok().body(favoriteService.toggleFavorite(id, user));
  }

  @Operation(
      summary = "내 즐겨찾기 게시글 목록 조회",
      description = "사용자가 즐겨찾기한 게시글 목록을 페이지 단위로 조회합니다.",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "즐겨찾기 목록 조회 성공"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청")
  })
  @GetMapping("/my")
  public ResponseEntity<Page<PostResponseDto>> getMyFavoritePosts(
      @Parameter(description = "조회할 페이지 번호 (0부터 시작)", example = "0")
      @RequestParam(defaultValue = "0") @PositiveOrZero int page,
      @Parameter(description = "페이지 당 게시글 개수", example = "10")
      @RequestParam(defaultValue = "10") @PositiveOrZero int size,
      @AuthenticatedUser User user
  ) {
    return ResponseEntity.ok()
        .body(favoriteService.getMyFavoritePosts(PageRequest.of(page, size), user));
  }
}
