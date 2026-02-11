package com.cheftory.api.exception;

import com.cheftory.api.user.exception.UserErrorCode;
import com.cheftory.api.voicecommand.exception.VoiceCommandErrorCode;
import java.util.List;

/**
 * 에러 코드를 정의하는 인터페이스.
 *
 * <p>모든 에러는 이 인터페이스를 구현해야 합니다.</p>
 */
public interface Error {
    /**
     * 에러 코드를 반환합니다.
     *
     * @return 에러 코드
     */
    String getErrorCode();

    /**
     * 에러 메시지를 반환합니다.
     *
     * @return 에러 메시지
     */
    String getMessage();

    /**
     * 에러 코드 이름으로부터 Error 인스턴스를 찾습니다.
     *
     * @param codeName 에러 코드 이름
     * @return Error 인스턴스 (찾지 못하면 UNKNOWN_ERROR)
     */
    static Error resolveErrorCode(String codeName) {
        List<Class<? extends Enum<?>>> enums = List.of(UserErrorCode.class, VoiceCommandErrorCode.class);

        for (Class<? extends Enum<?>> enumClass : enums) {
            for (Enum<?> e : enumClass.getEnumConstants()) {
                if (e.name().equals(codeName) && e instanceof Error ec) {
                    return ec;
                }
            }
        }

        return GlobalErrorCode.UNKNOWN_ERROR;
    }
}
