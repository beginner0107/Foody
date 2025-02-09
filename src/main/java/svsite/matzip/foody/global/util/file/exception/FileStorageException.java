package svsite.matzip.foody.global.util.file.exception;

import svsite.matzip.foody.global.exception.support.CustomException;
import svsite.matzip.foody.global.exception.support.ErrorCode;

public class FileStorageException extends CustomException {

  @Override
  public ErrorCode getErrorCode() {
    return super.getErrorCode();
  }

  public FileStorageException() {
    super();
  }

  public FileStorageException(String message) {
    super(message);
  }

  public FileStorageException(String message, Throwable cause) {
    super(message, cause);
  }

  public FileStorageException(ErrorCode errorCode) {
    super(errorCode);
  }

  public FileStorageException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
