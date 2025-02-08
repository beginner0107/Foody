package svsite.matzip.foody.domain.post.api;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.domain.post.api.dto.request.CreatePostDto;
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
      , description = "사용자는 맛집 위치 및 설명에 대한 글을 등록합니다.")
  @PostMapping("/posts")
  public ResponseEntity<PostResponseDto> createPost(@RequestBody @Valid CreatePostDto createPostDto
      , @AuthenticatedUser User user) {
    return ResponseEntity.status(CREATED).body(postService.createPost(createPostDto, user));
  }
}
