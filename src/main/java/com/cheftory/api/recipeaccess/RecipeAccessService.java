package com.cheftory.api.recipeaccess;

import com.cheftory.api.recipe.RecipeService;
import com.cheftory.api.recipe.dto.FullRecipeInfo;
import com.cheftory.api.recipe.dto.RecipeOverview;
import com.cheftory.api.recipeaccess.dto.FullRecipeResponse;
import com.cheftory.api.recipeaccess.dto.RecipeAccessResponse;
import com.cheftory.api.recipeaccess.dto.SimpleAccessInfosResponse;
import com.cheftory.api.recipeviewstate.RecipeViewStateService;
import com.cheftory.api.recipeviewstate.dto.SimpleAccessInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class RecipeAccessService {
  private final RecipeService recipeService;
  private final RecipeViewStateService recipeViewStateService;

  public RecipeAccessResponse tryAccessRecipe(URI videoUrl, UUID userId) {
    UUID recipeId = recipeService.create(UriComponentsBuilder.fromUri(videoUrl).build());
    UUID recipeViewStateId = recipeViewStateService.create(userId, recipeId);
    return RecipeAccessResponse.from(recipeViewStateId);
  }

  public FullRecipeResponse accessFullRecipe(UUID recipeViewStateId) {
    SimpleAccessInfo simpleAccessInfo = recipeViewStateService.findRecipeViewState(recipeViewStateId);
    FullRecipeInfo fullRecipeInfo = recipeService.findFullRecipeInfo(simpleAccessInfo.getId());
    return FullRecipeResponse.of(fullRecipeInfo);
  }

  public SimpleAccessInfosResponse accessByRecentOrder(UUID userId) {
    List<SimpleAccessInfo> recipeSimpleAccessInfos = recipeViewStateService.find(userId);

    List<RecipeOverview> recipes = recipeService.findOverviewRecipes(
        recipeSimpleAccessInfos
            .stream()
            .map(SimpleAccessInfo::getId)
            .toList()
    );

    Map<UUID, SimpleAccessInfo> recipeViewStateInfoMap =
        recipeSimpleAccessInfos.stream()
            .collect(Collectors.toMap(
                SimpleAccessInfo::getId,
                Function.identity(),
                (a, b) -> b,
                HashMap::new
            ));

    List<com.cheftory.api.recipeaccess.dto.SimpleAccessInfo> simpleAccessInfos = new ArrayList<>();

    for(RecipeOverview recipe : recipes) {
      simpleAccessInfos.add(
          com.cheftory.api.recipeaccess.dto.SimpleAccessInfo.of(
              recipeViewStateInfoMap.get(recipe.getId())
              ,recipe
          )
      );
    }

    simpleAccessInfos = simpleAccessInfos.stream()
        .sorted(
            Comparator.comparing(com.cheftory.api.recipeaccess.dto.SimpleAccessInfo::getViewedAt).reversed()
        )
        .toList();

    return SimpleAccessInfosResponse.from(simpleAccessInfos);
  }
}
