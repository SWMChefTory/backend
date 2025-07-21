package com.cheftory.api.recipe.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class Recipe {
    @Id
    @UuidGenerator
    private UUID id;

    @Embedded
    @Column(nullable = false)
    private VideoInfo videoInfo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecipeStatus status;

    private String description;
    private Integer count;
    private LocalDateTime createdAt;

    public static Recipe preCompletedOf(VideoInfo videoInfo) {
        return Recipe.builder()
                .videoInfo(videoInfo)
                .status(RecipeStatus.READY)
                .count(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public Boolean isCompleted() {
        return RecipeStatus.COMPLETED.equals(status);
    }

    public Boolean isReady(){
        return RecipeStatus.READY.equals(status);
    }

    public Boolean isBanned(){
        return RecipeStatus.NOT_COOK_URL.equals(status);
    }

    public String getVideoId() {
        return videoInfo.getVideoId();
    }
}
