package com.cheftory.api.recipe.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class RecipeSubContentCreatedAt {
    private LocalDateTime captionCreatedAt;
    private LocalDateTime ingredientsCreatedAt;
    private LocalDateTime stepsCreatedAt;

    public static RecipeSubContentCreatedAt from(
            LocalDateTime captionCreatedAt
            , LocalDateTime ingredientsCreatedAt
            , LocalDateTime stepsCreatedAt) {
        return RecipeSubContentCreatedAt.builder()
                .captionCreatedAt(captionCreatedAt)
                .ingredientsCreatedAt(ingredientsCreatedAt)
                .stepsCreatedAt(stepsCreatedAt)
                .build();
    }

    @JsonIgnore
    public boolean isAllCreated() {
        return !ObjectUtils.isEmpty(captionCreatedAt)
                && !ObjectUtils.isEmpty(ingredientsCreatedAt)
                && !ObjectUtils.isEmpty(stepsCreatedAt);
    }
}
