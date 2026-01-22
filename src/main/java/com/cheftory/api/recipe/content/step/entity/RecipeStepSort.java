package com.cheftory.api.recipe.content.step.entity;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

public final class RecipeStepSort {
    public static final Sort STEP_ORDER_ASC = Sort.by(Direction.ASC, "stepOrder");
}
