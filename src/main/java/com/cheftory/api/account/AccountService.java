package com.cheftory.api.account;

import com.cheftory.api.auth.model.AuthToken;
import com.cheftory.api.auth.AuthService;
import com.cheftory.api.user.entity.Gender;
import com.cheftory.api.user.UserService;
import com.cheftory.api.user.entity.Provider;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccountService {

    private final AuthService authService;
    private final UserService userService;

    public AccountService(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    public AuthToken loginWithOAuth(String token, Provider provider) {
        String email = authService.extractEmailFromOAuthToken(token, provider);
        UUID userId = userService.getUserIdByEmail(email);

        AuthToken authToken = authService.createAuthToken(userId);
        authService.saveRefreshToken(userId, authToken.getRefreshToken());
        return authToken;
    }

    public AuthToken signupWithOAuth(String token, Provider provider, String nickname, Gender gender) {
        String email = authService.extractEmailFromOAuthToken(token, provider);
        UUID userId = userService.create(email, nickname, provider, gender);

        AuthToken authToken = authService.createAuthToken(userId);
        authService.saveRefreshToken(userId, authToken.getRefreshToken());
        return authToken;
    }

    public AuthToken refresh(String refreshToken) {
        UUID userId = authService.extractUserIdFromRefreshToken(refreshToken);

        AuthToken authToken = authService.createAuthToken(userId);
        authService.updateRefreshToken(userId, refreshToken, authToken.getRefreshToken());
        return authToken;
    }

    public void logout(String refreshToken) {
        UUID userId = authService.extractUserIdFromRefreshToken(refreshToken);
        authService.deleteRefreshToken(userId, refreshToken);
    }
}
