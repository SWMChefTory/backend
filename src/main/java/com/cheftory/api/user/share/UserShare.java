package com.cheftory.api.user.share;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.MarketScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(uniqueConstraints = {
    @UniqueConstraint(name = "uq_user_share_date", columnNames = {"user_id", "shared_at"})
})
public class UserShare extends MarketScope {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private LocalDate sharedAt;

    @Column(nullable = false)
    private int count;

    @Version
    private long version;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static UserShare create(UUID userId, LocalDate sharedAt, Clock clock) {
        LocalDateTime now = clock.now();
        return new UserShare(UUID.randomUUID(), userId, sharedAt, 0, 0, now);
    }

    public void increase(int max) {
        if (this.count >= max) {
            throw new UserShareException(UserShareErrorCode.USER_SHARE_LIMIT_EXCEEDED);
        }
        this.count++;
    }

    public void decrease() {
        if (this.count > 0) {
            this.count--;
        }
    }
}
