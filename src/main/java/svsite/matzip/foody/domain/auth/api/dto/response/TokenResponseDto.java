package svsite.matzip.foody.domain.auth.api.dto.response;

import lombok.Builder;

public record TokenResponseDto(
    String accessToken,
    String refreshToken
) {
  @Builder
  public TokenResponseDto {}
}
