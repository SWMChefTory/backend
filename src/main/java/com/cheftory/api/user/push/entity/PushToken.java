package com.cheftory.api.user.push.entity;

import com.cheftory.api._common.Clock;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table
public class PushToken {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PushTokenProvider provider;

    @Column(nullable = false, length = 255)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PushTokenPlatform platform;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PushTokenStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime lastSeenAt;

    public static PushToken create(
            UUID userId, PushTokenProvider provider, String token, PushTokenPlatform platform, Clock clock) {
        LocalDateTime now = clock.now();
        return new PushToken(
                UUID.randomUUID(), userId, provider, token, platform, PushTokenStatus.ACTIVE, now, now, now);
    }

    public void assignTo(UUID userId, PushTokenPlatform platform, Clock clock) {
        LocalDateTime now = clock.now();
        this.userId = userId;
        this.platform = platform;
        this.status = PushTokenStatus.ACTIVE;
        this.updatedAt = now;
        this.lastSeenAt = now;
    }

    public void deactivate(Clock clock) {
        LocalDateTime now = clock.now();
        this.status = PushTokenStatus.INACTIVE;
        this.updatedAt = now;
    }
}
