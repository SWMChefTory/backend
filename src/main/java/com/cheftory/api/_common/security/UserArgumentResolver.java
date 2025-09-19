package com.cheftory.api._common.security;

import com.cheftory.api.account.auth.exception.AuthErrorCode;
import com.cheftory.api.account.auth.exception.AuthException;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(UserPrincipal.class);
  }

  @Override
  public Object resolveArgument(
      @NonNull MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      @NonNull NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {

    var authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {
      throw new AuthException(AuthErrorCode.INVALID_USER);
    }

    Object principal = authentication.getPrincipal();

    if (!(principal instanceof UUID)) {
      throw new AuthException(AuthErrorCode.INVALID_USER);
    }

    return principal;
  }
}
