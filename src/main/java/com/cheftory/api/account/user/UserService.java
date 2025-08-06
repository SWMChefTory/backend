package com.cheftory.api.account.user;

import com.cheftory.api.account.user.entity.Gender;
import com.cheftory.api.account.user.entity.Provider;
import com.cheftory.api.account.user.entity.UserStatus;
import com.cheftory.api.account.user.entity.User;
import com.cheftory.api.account.user.exception.UserErrorCode;
import com.cheftory.api.account.user.exception.UserException;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public User getByProviderAndProviderSub(Provider provider, String providerSub) {
    return userRepository.findByProviderAndProviderSubAndUserStatus(provider, providerSub, UserStatus.ACTIVE)
        .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
  }

  public UUID create(
      String nickname,
      Gender gender,
      LocalDate dateOfBirth,
      Provider provider,
      String providerSub
  ) {
    Optional<User> existUser = userRepository.findByProviderAndProviderSubAndUserStatus(provider, providerSub, UserStatus.ACTIVE);
    if (existUser.isPresent()) {
      throw new UserException(UserErrorCode.USER_ALREADY_EXIST);
    }

    User user = User.create(nickname, gender, dateOfBirth, provider, providerSub);
    return userRepository.save(user).getId();
  }

  public User get(UUID id) {
    return userRepository.findByIdAndUserStatus(id, UserStatus.ACTIVE)
        .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
  }

  @Transactional
  public void deleteUser(UUID id) {
    User user = userRepository.findByIdAndUserStatus(id, UserStatus.ACTIVE)
        .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

    user.changeStatus(UserStatus.DELETED);
    userRepository.save(user);
  }

  public boolean exists(UUID id) {
    return userRepository.existsById(id);
  }
}
