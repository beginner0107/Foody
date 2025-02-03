package svsite.matzip.foody.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import svsite.matzip.foody.domain.auth.api.dto.request.AuthRequestDto;
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.domain.auth.repository.UserRepository;
import svsite.matzip.foody.global.exception.errorCode.ErrorCodes;
import svsite.matzip.foody.global.exception.support.CustomException;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public Long signup(AuthRequestDto authRequestDto) {
    if (userRepository.findByEmail(authRequestDto.email()).isPresent()) {
      throw new CustomException(ErrorCodes.USER_EMAIL_DUPLICATE);
    }
    String hashedPassword = passwordEncoder.encode(authRequestDto.password());
    User user = User.signup(authRequestDto, hashedPassword);
    userRepository.save(user);
    return user.getId();
  }
}
