package svsite.matzip.foody.global.util.file.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
  String saveFile(MultipartFile file);
  void deleteFile(String fileName);
  String getFileUrl(String fileName);
}
