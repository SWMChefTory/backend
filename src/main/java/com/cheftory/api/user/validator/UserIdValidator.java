package com.cheftory.api.user.validator;

import com.cheftory.api.user.UserService;
import com.cheftory.api.user.exception.UserErrorCode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 유저 ID 검증 클래스
 *
 * <p>ExistsUserId 어노테이션을 사용하여 유저 ID 존재 여부를 검증합니다.</p>
 */
@Component
@RequiredArgsConstructor
public class UserIdValidator implements ConstraintValidator<ExistsUserId, UUID> {

    private final UserService userService;

    /**
     * 유저 ID 존재 여부 검증
     *
     * @param userId 검증할 유저 ID
     * @param context 제약 조건 검증 컨텍스트
     * @return 유저가 존재하면 true, 아니면 false
     */
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
