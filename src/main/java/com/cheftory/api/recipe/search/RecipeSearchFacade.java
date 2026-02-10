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
import com.cheftory.api.search.exception.SearchException;
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

/**
 * 레시피 검색 퍼사드.
 *
 * <p>OpenSearch 검색 결과를 레시피 개요 정보로 변환하여 제공합니다.</p>
 */
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

    /**
     * 검색어로 레시피를 검색하고 개요 정보를 반환합니다.
     *
     * @param query 검색어
     * @param userId 사용자 ID
     * @param cursor 페이징 커서
     * @return 레시피 개요 목록
     * @throws SearchException 검색 실패 시
     */
    public CursorPage<RecipeOverview> searchRecipes(String query, UUID userId, String cursor) throws SearchException {
        CursorPage<UUID> recipeIdsPage = recipeSearchPort.searchRecipeIds(userId, query, cursor);

        List<RecipeInfo> recipes = recipeInfoService.gets(recipeIdsPage.items());

        List<RecipeOverview> items = makeOverviews(recipes, userId);
        return CursorPage.of(items, recipeIdsPage.nextCursor());
    }

    private List<RecipeOverview> makeOverviews(List<RecipeInfo> recipes, UUID userId) {
        List<UUID> recipeIds = recipes.stream().map(RecipeInfo::getId).toList();

        Map<UUID, RecipeYoutubeMeta> youtubeMetaMap = recipeYoutubeMetaService.gets(recipeIds).stream()
                .collect(Collectors.toMap(RecipeYoutubeMeta::getRecipeId, Function.identity()));

        Map<UUID, RecipeDetailMeta> detailMetaMap = recipeDetailMetaService.getIn(recipeIds).stream()
                .collect(Collectors.toMap(RecipeDetailMeta::getRecipeId, Function.identity()));

        Map<UUID, List<RecipeTag>> tagsMap =
                recipeTagService.gets(recipeIds).stream().collect(Collectors.groupingBy(RecipeTag::getRecipeId));

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
