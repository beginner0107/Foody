package svsite.matzip.foody.domain.auth.service;

import static svsite.matzip.foody.global.constant.Constant.EMAIL;
import static svsite.matzip.foody.global.exception.errorCode.ErrorCodes.USER_WRONG_PASSWORD;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import svsite.matzip.foody.domain.auth.api.dto.request.AuthRequestDto;
import svsite.matzip.foody.domain.auth.api.dto.request.EditProfileDto;
import svsite.matzip.foody.domain.auth.api.dto.response.ProfileResponseDto;
import svsite.matzip.foody.domain.auth.api.dto.response.TokenResponseDto;
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.domain.auth.repository.UserRepository;
import svsite.matzip.foody.global.exception.errorCode.ErrorCodes;
import svsite.matzip.foody.global.exception.support.CustomException;
import svsite.matzip.foody.global.util.jwt.JwtUtil;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

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

  @Transactional
  public TokenResponseDto signin(AuthRequestDto authRequestDto) {
    User user = userRepository.findByEmail(authRequestDto.email())
        .orElseThrow(() -> new CustomException(ErrorCodes.USER_NOT_FOUND));

    if (!passwordEncoder.matches(authRequestDto.password(), user.getPassword())) {
      throw new CustomException(USER_WRONG_PASSWORD);
    }
    TokenResponseDto tokenDto = getTokens(authRequestDto.email());
    updateHashedRefreshToken(user, tokenDto.refreshToken());
    return tokenDto;
  }

  @Transactional
  public TokenResponseDto getTokens(String email) {
    Map<String, Object> payload = new HashMap<>();
    payload.put(EMAIL, email);
    String accessToken = jwtUtil.generateAccessToken(payload);
    String refreshToken = jwtUtil.generateRefreshToken(payload);
    return new TokenResponseDto(accessToken, refreshToken);
  }

  private void updateHashedRefreshToken(User user, String refreshToken) {
    String hashedRefreshToken = passwordEncoder.encode(refreshToken);
    user.updateHashedRefreshToken(hashedRefreshToken);
  }

  @Transactional
  public TokenResponseDto refreshToken(User user) {
    TokenResponseDto tokenDto = getTokens(user.getEmail());
    updateHashedRefreshToken(user, tokenDto.refreshToken());
    return tokenDto;
  }

  @Transactional
  public long deleteRefreshToken(User user) {
    user.updateHashedRefreshToken(null);
    return user.getId();
  }

  @Transactional(readOnly = true)
  public ProfileResponseDto getProfile(User user) {
    return ProfileResponseDto.from(user);
  }

  @Transactional
  public ProfileResponseDto editProfile(EditProfileDto editProfileDto, User user) {
    user.editProfile(editProfileDto);
    return ProfileResponseDto.from(user);
  }

  @Transactional
  public long deleteAccount(User user) {
    userRepository.delete(user);
    return user.getId();
  }
}
