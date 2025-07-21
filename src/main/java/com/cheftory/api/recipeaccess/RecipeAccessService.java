package com.cheftory.api.recipeaccess;

import com.cheftory.api.recipe.RecipeService;
import com.cheftory.api.recipe.dto.FullRecipeInfo;
import com.cheftory.api.recipe.dto.RecipeOverview;
import com.cheftory.api.recipeaccess.dto.FullRecipeResponse;
import com.cheftory.api.recipeaccess.dto.RecipeAccessResponse;
import com.cheftory.api.recipeaccess.dto.SimpleAccessInfo;
import com.cheftory.api.recipeaccess.dto.SimpleAccessInfosResponse;
import com.cheftory.api.recipeviewstate.RecipeViewStateService;
import com.cheftory.api.recipeviewstate.dto.ViewStateInfo;
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
    ViewStateInfo viewStateInfo = recipeViewStateService.findRecipeViewState(recipeViewStateId);
    FullRecipeInfo fullRecipeInfo = recipeService.findFullRecipeInfo(viewStateInfo.getId());
    return FullRecipeResponse.of(fullRecipeInfo);
  }

  public SimpleAccessInfosResponse accessByRecentOrder(UUID userId) {
    List<ViewStateInfo> recipeViewStateInfos = recipeViewStateService.find(userId);

    List<RecipeOverview> recipes = recipeService.findOverviewRecipes(
        recipeViewStateInfos
            .stream()
            .map(ViewStateInfo::getId)
            .toList()
    );

    Map<UUID, ViewStateInfo> recipeViewStateInfoMap =
        recipeViewStateInfos.stream()
            .collect(Collectors.toMap(
                ViewStateInfo::getId,
                Function.identity(),
                (a, b) -> b,
                HashMap::new
            ));

    List<SimpleAccessInfo> simpleAccessInfos = new ArrayList<>();

    for(RecipeOverview recipe : recipes) {
      simpleAccessInfos.add(
          SimpleAccessInfo.of(
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
