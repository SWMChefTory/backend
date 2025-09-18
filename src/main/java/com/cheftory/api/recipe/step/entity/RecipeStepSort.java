package com.cheftory.api.recipe.step.entity;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

public class RecipeStepSort {
  public static final Sort STEP_ORDER_ASC = Sort.by(Direction.ASC, "stepOrder");
}
