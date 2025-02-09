package svsite.matzip.foody.global.util.file.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import svsite.matzip.foody.global.util.file.exception.FileStorageException;

@Slf4j
@Component
@Profile("dev")
public class LocalFileStorageService implements FileStorageService {

  @Value("${file.upload-dir}")
  private String uploadDir;

  @Override
  public String saveFile(MultipartFile file) {
    try {
      String fileName = UUID.randomUUID() + getFileExtension(
          Objects.requireNonNull(file.getOriginalFilename()));
      File destination = new File(uploadDir, fileName);
      file.transferTo(destination);
      return fileName;
    } catch (IOException e) {
      throw new FileStorageException("파일 저장 중 오류 발생", e);
    }
  }

  @Override
  public void deleteFile(String fileName) {
    Path path = Paths.get(uploadDir, fileName);
    try {
      Files.delete(path);
    } catch (NoSuchFileException e) {
      log.warn("삭제하려는 파일 '{}'이 존재하지 않습니다.", fileName);
    } catch (IOException e) {
      throw new FileStorageException("파일 삭제 중 오류가 발생했습니다.", e);
    }
  }

  @Override
  public String getFileUrl(String fileName) {
    return "/uploads/" + fileName;
  }

  private String getFileExtension(String filename) {
    return filename.substring(filename.lastIndexOf("."));
  }
}
