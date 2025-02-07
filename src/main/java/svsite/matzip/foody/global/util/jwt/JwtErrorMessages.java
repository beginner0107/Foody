package svsite.matzip.foody.global.util.jwt;

public final class JwtErrorMessages {
  public static final String TOKEN_EXPIRED = "토큰이 만료되었습니다.";
  public static final String TOKEN_UNSUPPORTED = "지원하지 않는 JWT 형식입니다.";
  public static final String TOKEN_MALFORMED = "손상된 JWT 토큰입니다.";
  public static final String TOKEN_INVALID_SIGNATURE = "서명 검증에 실패한 JWT 토큰입니다.";
  public static final String TOKEN_INVALID = "유효하지 않은 JWT 토큰입니다.";
  public static final String TOKEN_HEADER_INVALID = "유효하지 않은 JWT 토큰 헤더입니다.";
  public static final String REFRESH_TOKEN_INVALID = "유효하지 않은 리프레시 토큰 입니다.";

  private JwtErrorMessages() {}
}
