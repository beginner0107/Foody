package svsite.matzip.foody.global.exception.errorCode;

import org.springframework.http.HttpStatus;
import svsite.matzip.foody.global.exception.support.CustomException;
import svsite.matzip.foody.global.exception.support.ErrorCode;

public enum ErrorCodes implements ErrorCode {
  USER_NOT_FOUND("해당 회원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  USER_EMAIL_DUPLICATE("회원 이메일이 중복됩니다.", HttpStatus.CONFLICT),
  USER_WRONG_ID_OR_PASSWORD("아이디 혹은 비밀번호가 잘못되었습니다.", HttpStatus.BAD_REQUEST),
  USER_WRONG_PASSWORD("비밀번호가 잘못되었습니다.", HttpStatus.BAD_REQUEST),


  POST_NOT_FOUND("해당 게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  POST_CONTENT_NEED("게시글의 내용을 입력해주세요.", HttpStatus.BAD_REQUEST),
  POST_NOT_MATCHING_USER("게시글 작성자만 게시글을 변경 가능합니다.", HttpStatus.BAD_REQUEST),

  FAVORITE_DUPLICATE("즐겨 찾기를 중복으로 할 수 없습니다.", HttpStatus.CONFLICT),

  FILE_SIZE_OUT("파일 크기는 최대 20MB 입니다", HttpStatus.CONFLICT),
  ;

  private final String message;
  private final HttpStatus httpStatus;

  ErrorCodes(String message, HttpStatus httpStatus) {
    this.message = message;
    this.httpStatus = httpStatus;
  }

  @Override
  public String defaultMessage() {
    return this.message;
  }

  @Override
  public HttpStatus defaultHttpStatus() {
    return this.httpStatus;
  }

  @Override
  public CustomException defaultException() {
    return new CustomException(this);
  }

  @Override
  public CustomException defaultException(Throwable cause) {
    return new CustomException(this, cause);
  }
}
