package com.cheftory.api.recipe.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.recipe.bookmark.RecipeBookmarkService;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmark;
import com.cheftory.api.recipe.content.detailMeta.RecipeDetailMetaService;
import com.cheftory.api.recipe.content.detailMeta.entity.RecipeDetailMeta;
import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.entity.RecipeStatus;
import com.cheftory.api.recipe.content.tag.RecipeTagService;
import com.cheftory.api.recipe.content.tag.entity.RecipeTag;
import com.cheftory.api.recipe.content.youtubemeta.RecipeYoutubeMetaService;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeMetaType;
import com.cheftory.api.recipe.dto.RecipeOverview;
import com.cheftory.api.search.exception.SearchException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecipeSearchFacade Tests")
class RecipeSearchFacadeTest {

    @Mock
    private RecipeSearchPort recipeSearchPort;

    @Mock
    private RecipeBookmarkService recipeBookmarkService;

    @Mock
    private RecipeYoutubeMetaService recipeYoutubeMetaService;

    @Mock
    private RecipeDetailMetaService recipeDetailMetaService;

    @Mock
    private RecipeTagService recipeTagService;

    @Mock
    private RecipeInfoService recipeInfoService;

    @InjectMocks
    private RecipeSearchFacade recipeSearchFacade;

    @Test
    @DisplayName("커서 기반 검색 결과를 반환한다")
    void shouldSearchRecipesWithCursor() throws SearchException {
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        String cursor = "cursor-1";
        String nextCursor = "cursor-2";

        doReturn(CursorPage.of(List.of(recipeId), nextCursor))
                .when(recipeSearchPort)
                .searchRecipeIds(userId, "김치찌개", cursor);

        RecipeInfo recipeInfo = mock(RecipeInfo.class);
        doReturn(recipeId).when(recipeInfo).getId();
        doReturn(RecipeStatus.SUCCESS).when(recipeInfo).getRecipeStatus();
        doReturn(100).when(recipeInfo).getViewCount();
        doReturn(LocalDateTime.now()).when(recipeInfo).getCreatedAt();
        doReturn(LocalDateTime.now()).when(recipeInfo).getUpdatedAt();
        doReturn(5L).when(recipeInfo).getCreditCost();
        doReturn(List.of(recipeInfo)).when(recipeInfoService).gets(anyList());

        RecipeYoutubeMeta youtubeMeta = mock(RecipeYoutubeMeta.class);
        doReturn(recipeId).when(youtubeMeta).getRecipeId();
        doReturn("title").when(youtubeMeta).getTitle();
        doReturn("channel").when(youtubeMeta).getChannelTitle();
        doReturn("video").when(youtubeMeta).getVideoId();
        doReturn(URI.create("https://example.com/video")).when(youtubeMeta).getVideoUri();
        doReturn(URI.create("https://example.com/thumb")).when(youtubeMeta).getThumbnailUrl();
        doReturn(120).when(youtubeMeta).getVideoSeconds();
        doReturn(YoutubeMetaType.NORMAL).when(youtubeMeta).getType();

        RecipeDetailMeta detailMeta = mock(RecipeDetailMeta.class);
        doReturn(recipeId).when(detailMeta).getRecipeId();
        doReturn("desc").when(detailMeta).getDescription();
        doReturn(2).when(detailMeta).getServings();
        doReturn(30).when(detailMeta).getCookTime();

        RecipeTag tag = mock(RecipeTag.class);
        doReturn(recipeId).when(tag).getRecipeId();
        doReturn("한식").when(tag).getTag();

        RecipeBookmark bookmark = mock(RecipeBookmark.class);
        doReturn(recipeId).when(bookmark).getRecipeId();

        doReturn(List.of(youtubeMeta)).when(recipeYoutubeMetaService).getByRecipes(List.of(recipeId));
        doReturn(List.of(detailMeta)).when(recipeDetailMetaService).getIn(List.of(recipeId));
        doReturn(List.of(tag)).when(recipeTagService).getIn(List.of(recipeId));
        doReturn(List.of(bookmark)).when(recipeBookmarkService).gets(List.of(recipeId), userId);

        CursorPage<RecipeOverview> result = recipeSearchFacade.searchRecipes("김치찌개", userId, cursor);

        assertThat(result.items()).hasSize(1);
        assertThat(result.nextCursor()).isEqualTo(nextCursor);
        verify(recipeSearchPort).searchRecipeIds(userId, "김치찌개", cursor);
        verify(recipeInfoService).gets(anyList());
    }
}
