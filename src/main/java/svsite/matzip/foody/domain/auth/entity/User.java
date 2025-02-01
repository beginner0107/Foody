package svsite.matzip.foody.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import svsite.matzip.foody.global.entity.BaseEntity;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email")
})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  private LoginType loginType;

  @Column(unique = true, nullable = false, length = 100)
  private String email;

  @Column(nullable = false, length = 200)
  private String password;

  @Column(nullable = false, length = 50)
  private String nickname;

  @Column(length = 255)
  private String imageUri;

  @Column(length = 255)
  private String kakaoImageUri;

  @Column(nullable = false, length = 50)
  private String RED;

  @Column(nullable = false, length = 50)
  private String YELLOW;

  @Column(nullable = false, length = 50)
  private String GREEN;

  @Column(nullable = false, length = 50)
  private String BLUE;

  @Column(nullable = false, length = 50)
  private String PURPLE;

  @Column(length = 255)
  private String hashedRefreshToken;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if(!(o instanceof User user)) return false;
    return id != null && id.equals(user.id);
  }
  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }
}
