package svsite.matzip.foody.global.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ImageConfig {
  @Value("${image.max-count}")
  private int maxImageCount;

  @Value("${image.max-size}")
  private long maxImageSize;
}
