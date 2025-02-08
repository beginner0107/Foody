package svsite.matzip.foody.domain.post.api;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.domain.post.api.dto.request.CreatePostDto;
import svsite.matzip.foody.domain.post.api.dto.request.UpdatePostDto;
import svsite.matzip.foody.domain.post.api.dto.response.MarkersResponseDto;
import svsite.matzip.foody.domain.post.api.dto.response.PostResponseDto;
import svsite.matzip.foody.domain.post.service.PostService;
import svsite.matzip.foody.global.auth.AuthenticatedUser;

@Tag(name = "Post", description = "맛집 게시글 관련 API")
@RestController
@RequiredArgsConstructor
public class PostController {

  private final PostService postService;

  @Operation(summary = "내가 등록한 맛집 마커 리스트 조회"
      , description = "사용자가 등록한 모든 맛집 좌표(마커)를 조회합니다."
      , security = @SecurityRequirement(name = "bearerAuth"))
  @GetMapping("/markers/my")
  public ResponseEntity<List<MarkersResponseDto>> getAllMarkers(@AuthenticatedUser User user) {
    return ResponseEntity.status(OK).body(postService.getAllMarkers(user));
  }

  @Operation(summary = "맛집 위치 및 설명에 대한 글을 등록"
      , description = "사용자는 맛집 위치 및 설명에 대한 글을 등록합니다."
      , security = @SecurityRequirement(name = "bearerAuth"))
  @PostMapping("/posts")
  public ResponseEntity<PostResponseDto> createPost(@RequestBody @Valid CreatePostDto createPostDto
      , @AuthenticatedUser User user) {
    return ResponseEntity.status(CREATED).body(postService.createPost(createPostDto, user));
  }

  @Operation(summary = "맛집 게시글 수정"
      , description = "사용자가 등록한 맛집 게시글을 수정합니다."
      , security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청")
  })
  @PatchMapping("/posts/{id}")
  public ResponseEntity<PostResponseDto> updatePost(@PathVariable("id") long id
      , @RequestBody @Valid UpdatePostDto updatePostDto
      , @AuthenticatedUser User user) {
    return ResponseEntity.status(OK).body(postService.updatePost(id, updatePostDto, user));
  }

  @Operation(summary = "맛집 게시글 삭제",
      description = "사용자가 등록한 맛집 게시글을 삭제합니다.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "게시글 삭제 성공"),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청")
  })
  @DeleteMapping("/posts/{id}")
  public ResponseEntity<Void> deletePost(@PathVariable("id") long id
      , @AuthenticatedUser User user) {
    postService.deletePost(id, user);
    return ResponseEntity.status(NO_CONTENT).body(null);
  }

  @Operation(
      summary = "내가 등록한 맛집 게시글 목록 조회",
      description = "사용자가 등록한 모든 맛집 게시글 목록을 페이지 단위로 조회합니다.",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 실패")
  })
  @GetMapping("/posts/my")
  public ResponseEntity<Page<PostResponseDto>> getPosts(
      @Parameter(description = "조회할 페이지 번호 (0부터 시작)", example = "0")
      @RequestParam(defaultValue = "0") @PositiveOrZero int page,
      @Parameter(description = "페이지 당 게시글 개수", example = "10")
      @RequestParam(defaultValue = "10") @PositiveOrZero int size,
      @AuthenticatedUser User user
  ) {
    return ResponseEntity.ok(postService.getPosts(PageRequest.of(page, size), user));
  }
}
