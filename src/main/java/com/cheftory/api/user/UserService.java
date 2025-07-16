package com.cheftory.api.user;

import com.cheftory.api.user.entity.Gender;
import com.cheftory.api.user.entity.Provider;
import com.cheftory.api.user.entity.Status;
import com.cheftory.api.user.entity.User;
import com.cheftory.api.user.exception.UserErrorCode;
import com.cheftory.api.user.exception.UserException;
import com.cheftory.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UUID getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .filter(u -> u.getStatus() != Status.DELETED)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND))
                .getId();
    }

    public UUID create(String email, String nickname, Provider provider, Gender gender) {
        if (userRepository.existsUserByEmail(email)) {
            throw new UserException(UserErrorCode.USER_ALREADY_EXIST);
        }

        User user = User.create(email, nickname, provider, gender);
        return userRepository.save(user).getId();
    }

    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        user.changeStatus(Status.DELETED);
        userRepository.save(user);
    }
}
