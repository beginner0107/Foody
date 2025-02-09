package svsite.matzip.foody.domain.image.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import svsite.matzip.foody.domain.auth.ControllerTestSupport;
import svsite.matzip.foody.global.util.file.exception.FileUploadException;

class ImageControllerTest extends ControllerTestSupport {

  @Test
  @DisplayName("이미지 파일 업로드에 성공한다")
  void uploadImagesSuccess() throws Exception {
    // given
    MockMultipartFile mockFile1 = new MockMultipartFile("files", "image1.jpg", "image/jpeg", "test-image-1".getBytes());
    MockMultipartFile mockFile2 = new MockMultipartFile("files", "image2.png", "image/png", "test-image-2".getBytes());

    // 서비스가 반환할 업로드된 파일 URL 목록 Mock 설정
    List<String> mockFileUrls = List.of("/uploads/image1.jpg", "/uploads/image2.png");
    when(fileUploadService.uploadFiles(any())).thenReturn(mockFileUrls);

    // when
    mockMvc.perform(multipart("/images")
            .file(mockFile1)
            .file(mockFile2)
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(mockFileUrls.size()))
        .andExpect(jsonPath("$[0]").value("/uploads/image1.jpg"))
        .andExpect(jsonPath("$[1]").value("/uploads/image2.png"));

    // then
    verify(fileUploadService).uploadFiles(any());
  }

  @Test
  @DisplayName("파일이 없을 경우 400 에러를 반환한다")
  void uploadImagesBadRequest() throws Exception {
    // when
    mockMvc.perform(multipart("/images")
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isBadRequest());

    // then
    verify(fileUploadService, never()).uploadFiles(any());
  }

  @Test
  @DisplayName("이미지 파일이 아닌 경우 400 에러를 반환한다")
  void uploadInvalidFileType() throws Exception {
    // given
    MockMultipartFile invalidFile = new MockMultipartFile("files", "document.txt", "text/plain", "invalid-file".getBytes());

    // Mock 예외 설정
    when(fileUploadService.uploadFiles(any())).thenThrow(new FileUploadException("허용되지 않은 파일 형식입니다."));

    // when
    mockMvc.perform(multipart("/images")
            .file(invalidFile)
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.message").value("허용되지 않은 파일 형식입니다."));

    // then
    verify(fileUploadService).uploadFiles(any());
  }
}
