package com.cheftory.api.recipe.entity;

import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
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

    private LocalDateTime captionCreatedAt;
    private LocalDateTime ingredientsCreatedAt;
    private LocalDateTime stepCreatedAt;

    public static Recipe preCompletedOf(VideoInfo videoInfo) {
        return Recipe.builder()
                .videoInfo(videoInfo)
                .status(RecipeStatus.PRE_COMPLETED)
                .count(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void isBanned() {
        if (status == RecipeStatus.NOT_COOK_URL) {
            throw new RecipeException(RecipeErrorCode.RECIPE_BANNED);
        }
    }

    public String getVideoId() {
        return null;
    }
}
