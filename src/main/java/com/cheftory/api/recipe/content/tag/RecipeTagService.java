package com.cheftory.api.recipe.content.tag;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.tag.entity.RecipeTag;
import com.cheftory.api.recipe.content.tag.repository.RecipeTagRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 레시피 태그 도메인의 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeTagService {
    private final RecipeTagRepository recipeTagRepository;
    private final Clock clock;

    /**
     * 레시피 ID로 태그 목록 조회
     *
     * @param recipeId 레시피 ID
     * @return 태그 목록
     */
    public List<RecipeTag> gets(UUID recipeId) {
        return recipeTagRepository.finds(recipeId);
    }

    /**
     * 여러 레시피 ID로 태그 목록 조회
     *
     * @param recipeIds 레시피 ID 목록
     * @return 태그 목록
     */
    public List<RecipeTag> gets(List<UUID> recipeIds) {
        return recipeTagRepository.finds(recipeIds);
    }

    /**
     * 레시피 태그 목록 생성
     *
     * <p>주어진 태그 문자열 목록을 기반으로 레시피 태그 엔티티를 생성하고 저장합니다.</p>
     *
     * @param recipeId 레시피 ID
     * @param tags 태그 문자열 목록
     */
    public void create(UUID recipeId, List<String> tags) {
        List<RecipeTag> recipeTags =
                tags.stream().map(tag -> RecipeTag.create(tag, recipeId, clock)).toList();
        recipeTagRepository.create(recipeTags);
    }
}
