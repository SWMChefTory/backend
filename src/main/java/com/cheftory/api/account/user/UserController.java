package com.cheftory.api.account.user;

import com.cheftory.api._common.security.UserPrincipal;
import com.cheftory.api.account.user.dto.UpdateUserResponse;
import com.cheftory.api.account.user.dto.UserResponse;
import com.cheftory.api.account.user.entity.User;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserService userService;

  @GetMapping("/me")
  public UserResponse getMyInfo(@UserPrincipal UUID userId) {
    User user = userService.get(userId);
    return new UserResponse(
        user.getNickname(),
        user.getGender(),
        user.getDateOfBirth(),
        user.getTermsOfUseAgreedAt(),
        user.getPrivacyAgreedAt(),
        user.getMarketingAgreedAt()
    );
  }

  @PatchMapping("/me")
  public UserResponse updateMyInfo(
      @UserPrincipal UUID userId,
      @RequestBody UpdateUserResponse request
  ) {
    User user = userService.update(userId, request.nickname(), request.gender(), request.dateOfBirth());
    return new UserResponse(
        user.getNickname(),
        user.getGender(),
        user.getDateOfBirth(),
        user.getTermsOfUseAgreedAt(),
        user.getPrivacyAgreedAt(),
        user.getMarketingAgreedAt()
    );
  }
}
