package svsite.matzip.foody.global.util.file.exception;

import svsite.matzip.foody.global.exception.support.CustomException;
import svsite.matzip.foody.global.exception.support.ErrorCode;

public class FileUploadException extends CustomException {

  public FileUploadException() {
  }

  public FileUploadException(String message) {
    super(message);
  }

  public FileUploadException(String message, Throwable cause) {
    super(message, cause);
  }

  public FileUploadException(ErrorCode errorCode) {
    super(errorCode);
  }

  public FileUploadException(ErrorCode errorCode,
      Throwable cause) {
    super(errorCode, cause);
  }
}
