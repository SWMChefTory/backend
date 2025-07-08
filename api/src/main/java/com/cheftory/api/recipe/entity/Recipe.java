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
    private VideoInfo videoInfo;

    @Enumerated(EnumType.STRING)
    private RecipeStatus status;

    private String description;
    private Integer count;
    private LocalDateTime createdAt;

    public static Recipe preCompletedOf(VideoInfo videoInfo) {
        return Recipe.builder()
                .videoInfo(videoInfo)
                .status(RecipeStatus.PRE_COMPLETED)
                .count(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public String getVideoId(){
        return null;
    }
}
