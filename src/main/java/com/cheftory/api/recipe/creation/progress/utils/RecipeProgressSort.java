package com.cheftory.api.recipe.creation.progress.utils;

import org.springframework.data.domain.Sort;

public final class RecipeProgressSort {
    public static final Sort CREATE_AT_ASC = Sort.by(Sort.Direction.ASC, "createdAt");
}
