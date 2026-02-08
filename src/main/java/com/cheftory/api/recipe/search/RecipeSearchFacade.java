package com.cheftory.api.recipe.search;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.recipe.bookmark.RecipeBookmarkService;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmark;
import com.cheftory.api.recipe.content.detailMeta.RecipeDetailMetaService;
import com.cheftory.api.recipe.content.detailMeta.entity.RecipeDetailMeta;
import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.tag.RecipeTagService;
import com.cheftory.api.recipe.content.tag.entity.RecipeTag;
import com.cheftory.api.recipe.content.youtubemeta.RecipeYoutubeMetaService;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.dto.RecipeOverview;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeSearchFacade {
    private final RecipeSearchPort recipeSearchPort;
    private final RecipeBookmarkService recipeBookmarkService;
    private final RecipeYoutubeMetaService recipeYoutubeMetaService;
    private final RecipeDetailMetaService recipeDetailMetaService;
    private final RecipeTagService recipeTagService;
    private final RecipeInfoService recipeInfoService;

    public CursorPage<RecipeOverview> searchRecipes(String query, UUID userId, String cursor) {
        CursorPage<UUID> recipeIdsPage = recipeSearchPort.searchRecipeIds(userId, query, cursor);

        List<RecipeInfo> recipes = recipeInfoService.gets(recipeIdsPage.items());

        List<RecipeOverview> items = makeOverviews(recipes, userId);
        return CursorPage.of(items, recipeIdsPage.nextCursor());
    }

    private List<RecipeOverview> makeOverviews(List<RecipeInfo> recipes, UUID userId) {
        List<UUID> recipeIds = recipes.stream().map(RecipeInfo::getId).toList();

        Map<UUID, RecipeYoutubeMeta> youtubeMetaMap = recipeYoutubeMetaService.getByRecipes(recipeIds).stream()
                .collect(Collectors.toMap(RecipeYoutubeMeta::getRecipeId, Function.identity()));

        Map<UUID, RecipeDetailMeta> detailMetaMap = recipeDetailMetaService.getIn(recipeIds).stream()
                .collect(Collectors.toMap(RecipeDetailMeta::getRecipeId, Function.identity()));

        Map<UUID, List<RecipeTag>> tagsMap =
                recipeTagService.getIn(recipeIds).stream().collect(Collectors.groupingBy(RecipeTag::getRecipeId));

        Map<UUID, RecipeBookmark> recipeViewStatusMap = recipeBookmarkService.gets(recipeIds, userId).stream()
                .collect(Collectors.toMap(RecipeBookmark::getRecipeId, Function.identity()));

        return recipes.stream()
                .map(recipe -> {
                    UUID recipeId = recipe.getId();
                    RecipeYoutubeMeta youtubeMeta = youtubeMetaMap.get(recipeId);
                    if (youtubeMeta == null) {
                        log.error("완료된 레시피의 유튜브 메타데이터 누락: recipeId={}", recipeId);
                        return null;
                    }

                    RecipeDetailMeta detailMeta = detailMetaMap.get(recipeId);
                    if (detailMeta == null) {
                        log.warn("레시피 상세 메타데이터 누락: recipeId={}", recipeId);
                    }

                    List<RecipeTag> tags = tagsMap.getOrDefault(recipeId, Collections.emptyList());
                    if (tags.isEmpty()) {
                        log.error("레시피의 태그 누락: recipeId={}", recipeId);
                    }

                    RecipeBookmark bookmark = recipeViewStatusMap.get(recipeId);
                    Boolean isViewed = bookmark != null;

                    return RecipeOverview.of(recipe, youtubeMeta, detailMeta, tags, isViewed);
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
