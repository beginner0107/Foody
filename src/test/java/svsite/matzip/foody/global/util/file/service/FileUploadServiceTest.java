package svsite.matzip.foody.global.util.file.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import svsite.matzip.foody.global.config.ImageConfig;
import svsite.matzip.foody.global.util.file.exception.FileStorageException;
import svsite.matzip.foody.global.util.file.exception.FileUploadException;

@ExtendWith(MockitoExtension.class)
class FileUploadServiceTest {

  @InjectMocks
  private FileUploadService fileUploadService;

  @Mock
  private FileStorageService fileStorageService;

  @Mock
  private ImageConfig imageConfig;

  @Test
  @DisplayName("이미지를 성공적으로 업로드한다")
  void uploadFiles_Success() {
    // given
    MultipartFile mockFile1 = new MockMultipartFile("file1", "image1.jpg", "image/jpeg", "test-image-1".getBytes());
    MultipartFile mockFile2 = new MockMultipartFile("file2", "image2.png", "image/png", "test-image-2".getBytes());

    when(imageConfig.getMaxImageCount()).thenReturn(5);
    when(imageConfig.getMaxImageSize()).thenReturn(10_000_000L);  // 10MB 제한
    when(fileStorageService.saveFile(any(MultipartFile.class))).thenReturn("saved-file.jpg");
    when(fileStorageService.getFileUrl(anyString())).thenReturn("http://localhost/uploads/saved-file.jpg");

    // when
    List<String> uploadedUrls = fileUploadService.uploadFiles(List.of(mockFile1, mockFile2));

    // then
    assertNotNull(uploadedUrls);
    assertEquals(2, uploadedUrls.size());
    assertTrue(uploadedUrls.contains("http://localhost/uploads/saved-file.jpg"));

    verify(fileStorageService, times(2)).saveFile(any(MultipartFile.class));
  }

  @Test
  @DisplayName("업로드 파일 개수가 제한을 초과할 경우 예외를 발생시킨다")
  void uploadFiles_FileCountExceedsLimit() {
    // given
    List<MultipartFile> mockFiles = List.of(
        new MockMultipartFile("file1", "image1.jpg", "image/jpeg", "test-image-1".getBytes()),
        new MockMultipartFile("file2", "image2.jpg", "image/jpeg", "test-image-2".getBytes()),
        new MockMultipartFile("file3", "image3.jpg", "image/jpeg", "test-image-3".getBytes())
    );

    when(imageConfig.getMaxImageCount()).thenReturn(2);  // 최대 2개 파일 제한

    // when & then
    FileUploadException exception = assertThrows(FileUploadException.class, () -> fileUploadService.uploadFiles(mockFiles));
    assertEquals("최대 2개의 파일만 업로드할 수 있습니다.", exception.getMessage());

    verify(fileStorageService, never()).saveFile(any(MultipartFile.class));
  }

  @Test
  @DisplayName("파일 형식이 올바르지 않을 경우 예외를 발생시킨다")
  void uploadFiles_InvalidFileType() {
    // given
    MultipartFile invalidFile = new MockMultipartFile("file", "document.txt", "text/plain", "invalid-file".getBytes());

    when(imageConfig.getMaxImageCount()).thenReturn(5);

    // when & then
    FileUploadException exception = assertThrows(FileUploadException.class, () -> fileUploadService.uploadFiles(List.of(invalidFile)));
    assertEquals("허용되지 않은 파일 형식입니다.", exception.getMessage());

    verify(fileStorageService, never()).saveFile(any(MultipartFile.class));
  }

  @Test
  @DisplayName("파일 크기가 제한을 초과할 경우 예외를 발생시킨다")
  void uploadFiles_FileSizeExceedsLimit() {
    // given
    MultipartFile largeFile = new MockMultipartFile("file", "large-image.jpg", "image/jpeg", new byte[15_000_000]);  // 15MB 크기 파일

    when(imageConfig.getMaxImageCount()).thenReturn(5);
    when(imageConfig.getMaxImageSize()).thenReturn(10_000_000L);  // 10MB 제한

    // when & then
    FileUploadException exception = assertThrows(FileUploadException.class, () -> fileUploadService.uploadFiles(List.of(largeFile)));
    assertEquals("파일 크기가 초과되었습니다. 최대 10000000 bytes까지 허용됩니다.", exception.getMessage());

    verify(fileStorageService, never()).saveFile(any(MultipartFile.class));
  }

  @Test
  @DisplayName("파일 저장 중 오류가 발생할 경우 예외를 발생시킨다")
  void uploadFiles_FileStorageError() {
    // given
    MultipartFile mockFile = new MockMultipartFile("file", "image.jpg", "image/jpeg", "test-image".getBytes());

    when(imageConfig.getMaxImageCount()).thenReturn(5);
    when(imageConfig.getMaxImageSize()).thenReturn(10_000_000L);
    when(fileStorageService.saveFile(mockFile)).thenThrow(new FileStorageException("파일 저장 중 오류 발생"));

    // when & then
    FileStorageException exception = assertThrows(FileStorageException.class, () -> fileUploadService.uploadFiles(List.of(mockFile)));
    assertEquals("파일 저장 중 오류 발생", exception.getMessage());
  }
}
