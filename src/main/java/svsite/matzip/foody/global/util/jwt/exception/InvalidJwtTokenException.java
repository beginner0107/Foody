package svsite.matzip.foody.global.util.jwt.exception;

import svsite.matzip.foody.global.exception.support.CustomException;
import svsite.matzip.foody.global.exception.support.ErrorCode;

public class InvalidJwtTokenException extends CustomException {

  public InvalidJwtTokenException() {
  }

  public InvalidJwtTokenException(String message) {
    super(message);
  }

  public InvalidJwtTokenException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidJwtTokenException(ErrorCode errorCode) {
    super(errorCode);
  }

  public InvalidJwtTokenException(ErrorCode errorCode,
      Throwable cause) {
    super(errorCode, cause);
  }
}
