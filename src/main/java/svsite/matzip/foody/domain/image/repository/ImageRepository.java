package svsite.matzip.foody.domain.image.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import svsite.matzip.foody.domain.image.entity.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
