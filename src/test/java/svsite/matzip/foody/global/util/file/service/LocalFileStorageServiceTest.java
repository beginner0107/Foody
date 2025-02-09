package svsite.matzip.foody.global.util.file.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import svsite.matzip.foody.global.util.file.exception.FileStorageException;

@ExtendWith(MockitoExtension.class)
class LocalFileStorageServiceTest {

  private LocalFileStorageService fileStorageService;

  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() {
    fileStorageService = new LocalFileStorageService();
    ReflectionTestUtils.setField(fileStorageService, "uploadDir", tempDir.toString());
  }

  @Test
  @DisplayName("파일을 성공적으로 저장한다")
  void saveFile_Success() throws Exception {
    // given
    MultipartFile mockFile = new MockMultipartFile("file", "image.jpg", "image/jpeg", "test-content".getBytes());

    // when
    String savedFileName = fileStorageService.saveFile(mockFile);

    // then
    assertNotNull(savedFileName, "저장된 파일 이름이 null이 아니어야 합니다.");
    Path savedFilePath = tempDir.resolve(savedFileName);
    assertTrue(Files.exists(savedFilePath), "저장된 파일이 존재해야 합니다.");
    assertEquals("test-content", Files.readString(savedFilePath), "파일 내용이 일치해야 합니다.");
  }

  @Test
  @DisplayName("파일 저장 중 오류가 발생하면 예외를 던진다")
  void saveFile_Failure() throws Exception {
    // given
    MultipartFile mockFile = mock(MultipartFile.class);

    when(mockFile.getOriginalFilename()).thenReturn("image.jpg");
    doThrow(IOException.class).when(mockFile).transferTo(any(File.class));

    // when & then
    FileStorageException exception = assertThrows(FileStorageException.class, () -> fileStorageService.saveFile(mockFile));
    assertEquals("파일 저장 중 오류 발생", exception.getMessage());
  }

  @Test
  @DisplayName("파일을 성공적으로 삭제한다")
  void deleteFile_Success() throws Exception {
    // given
    String fileName = "test-file.txt";
    Path filePath = Files.createFile(tempDir.resolve(fileName));

    // when
    fileStorageService.deleteFile(fileName);

    // then
    assertFalse(Files.exists(filePath), "파일이 삭제되어야 합니다.");
  }

  @Test
  @DisplayName("삭제하려는 파일이 존재하지 않으면 경고 로그를 출력한다")
  void deleteFile_FileNotFound() {
    // given
    String nonExistentFile = "non-existent-file.txt";

    // when & then
    fileStorageService.deleteFile(nonExistentFile);

    // 로그 확인은 어렵기 때문에 예외가 발생하지 않는지 검증
    assertDoesNotThrow(() -> fileStorageService.deleteFile(nonExistentFile));
  }

  @Test
  @DisplayName("파일 삭제 중 오류가 발생하면 예외를 던진다")
  void deleteFile_Failure() throws Exception {
    // given
    String fileName = "test-file.txt";
    Path filePath = tempDir.resolve(fileName);
    Files.createFile(filePath);

    // 파일 삭제 중 IOException 발생 시뮬레이션
    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.delete(any(Path.class))).thenThrow(IOException.class);

      // when & then
      FileStorageException exception = assertThrows(FileStorageException.class, () -> fileStorageService.deleteFile(fileName));
      assertEquals("파일 삭제 중 오류가 발생했습니다.", exception.getMessage());
    }
  }

  @Test
  @DisplayName("파일 URL을 반환한다")
  void getFileUrl() {
    // given
    String fileName = "image.jpg";

    // when
    String fileUrl = fileStorageService.getFileUrl(fileName);

    // then
    assertEquals("/uploads/" + fileName, fileUrl);
  }
}
