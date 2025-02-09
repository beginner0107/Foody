package svsite.matzip.foody.global.util.file.service;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.PAYLOAD_TOO_LARGE;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import svsite.matzip.foody.global.config.ImageConfig;
import svsite.matzip.foody.global.util.file.exception.FileUploadException;

@Service
@RequiredArgsConstructor
public class FileUploadService {

  private final FileStorageService fileStorageService;
  private final ImageConfig imageConfig;

  public List<String> uploadFiles(List<MultipartFile> files) {
    validateFileCount(files.size());

    return files.stream()
        .map(this::validateAndSaveFile)
        .collect(Collectors.toList());
  }

  private void validateFileCount(int size) {
    if (size > imageConfig.getMaxImageCount()) {
      throw new FileUploadException(
          "최대 " + imageConfig.getMaxImageCount() + "개의 파일만 업로드할 수 있습니다.", BAD_REQUEST
      );
    }
  }

  private String validateAndSaveFile(MultipartFile file) {
    if (!Objects.requireNonNull(file.getContentType()).startsWith("image/")) {
      throw new FileUploadException("허용되지 않은 파일 형식입니다.", UNSUPPORTED_MEDIA_TYPE);
    }

    if (file.getSize() > imageConfig.getMaxImageSize()) {
      throw new FileUploadException("파일 크기가 초과되었습니다. 최대 " + imageConfig.getMaxImageSize() + " bytes까지 허용됩니다.", PAYLOAD_TOO_LARGE);
    }

    String fileName = fileStorageService.saveFile(file);
    return fileStorageService.getFileUrl(fileName);
  }
}
