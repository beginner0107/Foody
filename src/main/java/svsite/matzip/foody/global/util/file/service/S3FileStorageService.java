package svsite.matzip.foody.global.util.file.service;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import svsite.matzip.foody.global.util.file.exception.FileStorageException;

@Slf4j
@Component
@Profile("prod") // 배포 환경에서만 실행
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {

  private final S3Client s3Client;

  @Value("${aws.s3.bucket-name}")
  private String bucketName;

  @Override
  public String saveFile(MultipartFile file) {
    try {
      String fileName = UUID.randomUUID() + getFileExtension(
          Objects.requireNonNull(file.getOriginalFilename()));

      PutObjectRequest request = PutObjectRequest.builder()
          .bucket(bucketName)
          .key(fileName)
          .contentType(file.getContentType())
          .build();

      s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

      return fileName;
    } catch (IOException e) {
      throw new FileStorageException("파일 저장 중 오류 발생", e);
    }
  }

  @Override
  public void deleteFile(String fileName) {
    try {
      s3Client.deleteObject(DeleteObjectRequest.builder()
          .bucket(bucketName)
          .key(fileName)
          .build());
    } catch (Exception e) {
      throw new FileStorageException("파일 삭제 중 오류 발생", e);
    }
  }

  @Override
  public String getFileUrl(String fileName) {
    return "https://" + bucketName + ".s3.amazonaws.com/" + fileName;
  }

  private String getFileExtension(String filename) {
    return filename.substring(filename.lastIndexOf("."));
  }
}
