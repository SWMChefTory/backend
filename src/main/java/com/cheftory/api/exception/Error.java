package com.cheftory.api.exception;

import com.cheftory.api.user.exception.UserErrorCode;
import com.cheftory.api.voicecommand.exception.VoiceCommandErrorCode;
import java.util.List;

public interface Error {
    String getErrorCode();

    String getMessage();

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
