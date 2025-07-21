package com.cheftory.api.user.validator;


import com.cheftory.api.user.UserService;
import com.cheftory.api.user.exception.UserErrorCode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserIdValidator implements ConstraintValidator<ExistsUserId, UUID> {

  private final UserService userService;

  @Override
  public boolean isValid(UUID userId, ConstraintValidatorContext context) {
    boolean exists = userService.exists(userId);

    if (!exists) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(UserErrorCode.USER_NOT_FOUND.name())
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}