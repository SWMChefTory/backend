package com.cheftory.api.account.user;

import com.cheftory.api.account.user.entity.Gender;
import com.cheftory.api.account.user.entity.Provider;
import com.cheftory.api.account.user.entity.Status;
import com.cheftory.api.account.user.entity.User;
import com.cheftory.api.account.user.exception.UserErrorCode;
import com.cheftory.api.account.user.exception.UserException;
import com.cheftory.api.account.user.repository.UserRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public User getByProviderAndProviderSub(Provider provider, String providerSub) {
    return userRepository.findByProviderAndProviderSub(provider, providerSub)
        .filter(u -> u.getStatus() != Status.DELETED)
        .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
  }

  public UUID create(
      String email,
      String nickname,
      Gender gender,
      LocalDate dateOfBirth,
      Provider provider,
      String providerSub
  ) {
    if (userRepository.existsUserByProviderAndProviderSub(provider, providerSub)) {
      throw new UserException(UserErrorCode.USER_ALREADY_EXIST);
    }

    User user = User.create(email, nickname, gender, dateOfBirth, provider, providerSub);
    return userRepository.save(user).getId();
  }

  public void deleteUser(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

    user.changeStatus(Status.DELETED);
    userRepository.save(user);
  }

  public boolean exists(UUID userId) {
    return userRepository.existsById(userId);
  }
}
