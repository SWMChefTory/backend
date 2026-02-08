package com.cheftory.api.user;

import com.cheftory.api._common.reponse.SuccessOnlyResponse;
import com.cheftory.api._common.security.UserPrincipal;
import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.user.dto.UserRequest;
import com.cheftory.api.user.dto.UserResponse;
import com.cheftory.api.user.entity.User;
import com.cheftory.api.user.exception.UserException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 유저 관련 API 요청을 처리하는 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    /**
     * 현재 로그인한 유저 정보 조회
     *
     * @param userId 인증된 유저 ID (@UserPrincipal로 주입됨)
     * @return 유저 정보 응답 DTO
     * @throws UserException 유저를 찾을 수 없을 때 USER_NOT_FOUND
     */
    @GetMapping("/me")
    public UserResponse getUser(@UserPrincipal UUID userId) {
        User user = userService.get(userId);
        return UserResponse.from(user);
    }

    /**
     * 현재 로그인한 유저 정보 수정
     *
     * @param userId 인증된 유저 ID (@UserPrincipal로 주입됨)
     * @param request 수정할 유저 정보 (닉네임, 성별, 생년월일)
     * @return 수정된 유저 정보 응답 DTO
     */
    @PatchMapping("/me")
    public UserResponse updateUser(@UserPrincipal UUID userId, @RequestBody UserRequest.Update request) {
        User user = userService.update(userId, request.nickname(), request.gender(), request.dateOfBirth());
        return UserResponse.from(user);
    }

    /**
     * 튜토리얼 완료 처리 및 크레딧 지급
     *
     * @param userId 인증된 유저 ID (@UserPrincipal로 주입됨)
     * @return 성공 메시지 응답 DTO
     * @throws UserException 유저를 찾을 수 없을 때 USER_NOT_FOUND
     * @throws UserException 이미 튜토리얼을 완료했을 때 TUTORIAL_ALREADY_FINISHED
     * @throws CheftoryException 크레딧 지급 실패 시 튜토리얼 상태 복구 후 예외 전파
     */
    @PostMapping("/tutorial")
    public SuccessOnlyResponse tutorial(@UserPrincipal UUID userId) {
        userService.tutorial(userId);
        return SuccessOnlyResponse.create();
    }
}
