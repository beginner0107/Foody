package svsite.matzip.foody.domain.image.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.global.auth.AuthenticatedUser;
import svsite.matzip.foody.global.util.file.service.FileUploadService;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
@Tag(name = "Image API", description = "이미지 업로드 및 관리 API")  // 태그 이름 추가
public class ImageController {

  private final FileUploadService fileUploadService;

  @Operation(
      summary = "이미지 업로드",
      description = "사용자가 여러 이미지를 업로드할 수 있는 API입니다.",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "업로드 성공",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = List.class, description = "업로드된 파일 URL 목록")
              )
          ),
          @ApiResponse(
              responseCode = "400",
              description = "업로드 실패 (잘못된 요청)",
              content = @Content(mediaType = "application/json")
          ),
          @ApiResponse(
              responseCode = "401",
              description = "인증 실패",
              content = @Content(mediaType = "application/json")
          )
      },
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @PostMapping
  public ResponseEntity<List<String>> uploadImages(
      @AuthenticatedUser User user,
      @Parameter(
          description = "업로드할 이미지 파일 목록",
          required = true,
          content = @Content(mediaType = "multipart/form-data")
      )
      @RequestParam("files") List<MultipartFile> files
  ) {
    List<String> fileUrls = fileUploadService.uploadFiles(files);
    return ResponseEntity.ok(fileUrls);
  }
}
