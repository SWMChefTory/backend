package com.cheftory.api.exception;

import com.cheftory.api.account.user.exception.UserErrorCode;
import com.cheftory.api.voicecommand.exception.VoiceCommandErrorCode;
import java.util.List;

public interface ErrorMessage {
  String getErrorCode();
  String getMessage();


  static ErrorMessage resolveErrorCode(String codeName) {
    List<Class<? extends Enum<?>>> enums = List.of(
        UserErrorCode.class,
        VoiceCommandErrorCode.class
    );

    for (Class<? extends Enum<?>> enumClass : enums) {
      for (Enum<?> e : enumClass.getEnumConstants()) {
        if (e.name().equals(codeName) && e instanceof ErrorMessage ec) {
          return ec;
        }
      }
    }

    return GlobalErrorCode.UNKNOWN_ERROR;
  }
}
