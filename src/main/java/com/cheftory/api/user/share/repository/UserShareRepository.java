package com.cheftory.api.user.share.repository;

import com.cheftory.api._common.Clock;
import com.cheftory.api.user.share.entity.UserShare;
import com.cheftory.api.user.share.exception.UserShareException;
import java.time.LocalDate;
import java.util.UUID;

public interface UserShareRepository {

    UserShare shareTx(UUID userShareId, int limit) throws UserShareException;

    void compensateTx(UUID userShareId, LocalDate sharedAt) throws UserShareException;

    UserShare create(UUID userId, Clock clock) throws UserShareException;
}
