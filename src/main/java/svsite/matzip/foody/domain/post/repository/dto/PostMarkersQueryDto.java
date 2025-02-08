package svsite.matzip.foody.domain.post.repository.dto;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import svsite.matzip.foody.domain.post.entity.MarkerColor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PostMarkersQueryDto {
  private Long id;
  private BigDecimal latitude;
  private BigDecimal longitude;
  private MarkerColor color;
  private Integer score;
}
