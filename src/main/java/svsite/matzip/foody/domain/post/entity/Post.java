package svsite.matzip.foody.domain.post.entity;

import static jakarta.persistence.FetchType.LAZY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.global.entity.BaseEntity;

@Entity
@Table(name = "post")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(precision = 10, scale = 6)
  private BigDecimal latitude;

  @Column(precision = 10, scale = 6)
  private BigDecimal longitude;

  @Enumerated(EnumType.STRING)
  private MarkerColor color;

  @Column(length = 255)
  private String address;
  @Column(nullable = false, length = 100)
  private String title;
  @Lob
  private String description;

  private LocalDateTime date;

  private Integer score;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if(!(o instanceof Post post)) return false;
    return id != null && id.equals(post.id);
  }
  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }
}

