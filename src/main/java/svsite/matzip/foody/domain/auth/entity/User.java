package svsite.matzip.foody.domain.auth.entity;

import static svsite.matzip.foody.domain.auth.entity.LoginType.EMAIL;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import svsite.matzip.foody.domain.auth.api.dto.request.AuthRequestDto;
import svsite.matzip.foody.domain.favorite.entity.Favorite;
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

  @Column(length = 50)
  private String nickname;

  @Column(length = 255)
  private String imageUri;

  @Column(length = 255)
  private String kakaoImageUri;

  @Column(length = 50)
  private String RED;

  @Column(length = 50)
  private String YELLOW;

  @Column(length = 50)
  private String GREEN;

  @Column(length = 50)
  private String BLUE;

  @Column(length = 50)
  private String PURPLE;

  @Column(length = 255)
  private String hashedRefreshToken;

  @OneToMany(mappedBy = "user", orphanRemoval = true)
  private List<Favorite> favorites;

  public static User signup(AuthRequestDto authDto, String hashedPassword) {
    return User.builder()
        .email(authDto.email())
        .password(hashedPassword)
        .loginType(EMAIL)
        .build();
  }

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

  public void updateHashedRefreshToken(String hashedRefreshToken) {
    this.hashedRefreshToken = hashedRefreshToken;
  }
}
