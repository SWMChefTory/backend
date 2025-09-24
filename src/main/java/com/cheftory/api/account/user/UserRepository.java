package com.cheftory.api.account.user;

import com.cheftory.api.account.user.entity.Provider;
import com.cheftory.api.account.user.entity.User;
import com.cheftory.api.account.user.entity.UserStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByProviderAndProviderSubAndUserStatus(
      Provider provider, String sub, UserStatus userStatus);

  Optional<User> findByIdAndUserStatus(UUID userId, UserStatus userStatus);
}
