package svsite.matzip.foody.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import svsite.matzip.foody.global.exception.response.ApiResponseError;
import svsite.matzip.foody.global.exception.support.CustomException;

@Slf4j
@RestControllerAdvice
public final class GlobalExceptionHandler {

  // CustomException 핸들링
  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ApiResponseError> handleCustomException(CustomException exception,
      HttpServletRequest request) {
    return handleException(
        "CUSTOM_ERROR", exception.getErrorCode().defaultHttpStatus(), exception.getMessage(),
        request, true
    );
  }

  // 유효성 검사 실패 예외
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponseError> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException exception, HttpServletRequest request) {

    return handleException(
        "VALIDATION_ERROR", HttpStatus.BAD_REQUEST, "유효성 검사가 실패했습니다.", request, false
    );
  }

  // 파일 업로드 크기 초과 예외
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ApiResponseError> handleMaxUploadSizeExceededException(
      MaxUploadSizeExceededException exception, HttpServletRequest request) {

    return handleException(
        "UPLOAD_SIZE_EXCEEDED", HttpStatus.PAYLOAD_TOO_LARGE, "업로드 가능한 최대 파일 크기를 초과했습니다.", request,
        false
    );
  }

  // 바인딩 예외
  @ExceptionHandler(BindException.class)
  public ResponseEntity<ApiResponseError> handleBindException(BindException exception,
      HttpServletRequest request) {
    String message =
        exception.getFieldError() != null ? exception.getFieldError().getDefaultMessage()
            : "요청 바인딩에 실패했습니다.";
    return handleException("BIND_ERROR", HttpStatus.BAD_REQUEST, message, request, false);
  }

  // 리소스 찾기 실패 예외
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ApiResponseError> handleNoResourceFoundException(
      NoResourceFoundException exception, HttpServletRequest request) {
    return handleException(
        "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다.", request, false
    );
  }

  // 필수 요청 파라미터 누락 예외
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiResponseError> handleMissingServletRequestParameterException(
      MissingServletRequestParameterException exception, HttpServletRequest request) {

    String message = String.format("필수 요청 파라미터 '%s'가 누락되었습니다.", exception.getParameterName());
    return handleException("MISSING_PARAMETER", HttpStatus.BAD_REQUEST, message, request, false);
  }

  // 요청 파라미터 타입 불일치 예외
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiResponseError> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException exception, HttpServletRequest request) {

    String message = String.format("파라미터 '%s'의 값 '%s'가 올바르지 않습니다.", exception.getName(),
        exception.getValue());
    return handleException("TYPE_MISMATCH", HttpStatus.BAD_REQUEST, message, request, false);
  }

  // 공통 예외 처리 메서드
  private ResponseEntity<ApiResponseError> handleException(
      String code, HttpStatus status, String message, HttpServletRequest request, boolean isError) {

    if (isError) {
      log.error("Exception 발생 - Path: {}, Message: {}", request.getRequestURI(), message);
    } else {
      log.warn("Exception 발생 - Path: {}, Message: {}", request.getRequestURI(), message);
    }

    ApiResponseError response = ApiResponseError.builder()
        .code(code)
        .status(status.value())
        .message(message)
        .timestamp(Instant.now())
        .path(request.getRequestURI())
        .build();

    return ResponseEntity.status(status).body(response);
  }
}
