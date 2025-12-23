package com.cheftory.api.recipe.dto;

import java.net.URI;
import java.util.UUID;

public sealed interface RecipeCreationTarget
    permits RecipeCreationTarget.User, RecipeCreationTarget.Crawler {

  URI uri();

  record User(URI uri, UUID userId) implements RecipeCreationTarget {}

  record Crawler(URI uri) implements RecipeCreationTarget {}
}
