package svsite.matzip.foody.global.util.file.exception;

import org.springframework.http.HttpStatus;

public class FileUploadException extends RuntimeException {
  private final HttpStatus status;

  public FileUploadException(String message, HttpStatus status) {
    super(message);
    this.status = status;
  }

  public HttpStatus getStatus() {
    return status;
  }
}
