package com.cheftory.api.recipe.content.info;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.I18nTranslator;
import com.cheftory.api._common.cursor.*;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoException;
import com.cheftory.api.recipe.content.info.repository.RecipeInfoRepository;
import com.cheftory.api.recipe.dto.RecipeCuisineType;
import com.cheftory.api.recipe.dto.RecipeInfoVideoQuery;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 레시피 기본 정보 도메인의 비즈니스 로직을 처리하는 서비스
 */
@Service
@Slf4j
public class RecipeInfoService {
    private final RecipeInfoRepository repository;
    private final Clock clock;
    private final I18nTranslator i18nTranslator;

    public RecipeInfoService(RecipeInfoRepository repository, Clock clock, I18nTranslator i18nTranslator) {
        this.repository = repository;
        this.clock = clock;
        this.i18nTranslator = i18nTranslator;
    }

    /**
     * 성공 상태의 레시피 정보 조회
     *
     * @param recipeId 레시피 ID
     * @return 레시피 정보 엔티티
     * @throws RecipeInfoException 레시피가 실패 상태이거나 차단된 상태일 때, 또는 찾을 수 없을 때
     */
    public RecipeInfo getSuccess(UUID recipeId) throws RecipeInfoException {
        RecipeInfo recipeInfo = repository.get(recipeId);

        if (recipeInfo.isFailed()) {
            throw new RecipeInfoException(RecipeInfoErrorCode.RECIPE_FAILED);
        }

        if (recipeInfo.isBlocked()) {
            throw new RecipeInfoException(RecipeInfoErrorCode.RECIPE_BANNED);
        }

        return recipeInfo;
    }

    /**
     * 레시피 조회수 증가
     *
     * @param recipeId 레시피 ID
     */
    public void increaseCount(UUID recipeId) {
        repository.increaseCount(recipeId);
    }

    /**
     * 레시피 기본 정보 생성
     *
     * @return 생성된 레시피 정보 엔티티
     */
    public RecipeInfo create() {
        RecipeInfo recipeInfo = RecipeInfo.create(clock);
        repository.create(recipeInfo);
        return recipeInfo;
    }

    /**
     * 진행 중인 레시피 목록 조회
     *
     * @param recipeIds 레시피 ID 목록
     * @return 진행 중인 레시피 정보 목록
     */
    public List<RecipeInfo> getProgresses(List<UUID> recipeIds) {
        return repository.getProgressRecipes(recipeIds);
    }

    /**
     * 레시피 정보 목록 조회
     *
     * @param recipeIds 레시피 ID 목록
     * @return 레시피 정보 목록
     */
    public List<RecipeInfo> gets(List<UUID> recipeIds) {
        return repository.gets(recipeIds);
    }

    @Deprecated
    public List<RecipeInfo> getValidRecipes(List<UUID> recipeIds) {
        return Stream.concat(repository.gets(recipeIds).stream(), repository.getProgressRecipes(recipeIds).stream())
                .toList();
    }

    /**
     * 인기 레시피 목록 조회 (커서 기반 페이징)
     *
     * @param cursor 페이징 커서
     * @param videoQuery 비디오 쿼리 조건
     * @return 인기 레시피 커서 페이지
     * @throws CursorException 커서 처리 중 예외 발생 시
     */
    public CursorPage<RecipeInfo> getPopulars(String cursor, RecipeInfoVideoQuery videoQuery) throws CursorException {
        boolean first = (cursor == null || cursor.isBlank());
        return first ? repository.popularFirst(videoQuery) : repository.popularKeyset(videoQuery, cursor);
    }

    /**
     * 레시피 상태를 성공으로 변경
     *
     * @param recipeId 레시피 ID
     * @return 업데이트된 레시피 정보 엔티티
     * @throws RecipeInfoException 레시피를 찾을 수 없을 때
     */
    public RecipeInfo success(UUID recipeId) throws RecipeInfoException {
        return repository.success(recipeId, clock);
    }

    /**
     * 레시피 상태를 실패로 변경
     *
     * @param recipeId 레시피 ID
     * @return 업데이트된 레시피 정보 엔티티
     * @throws RecipeInfoException 레시피를 찾을 수 없을 때
     */
    public RecipeInfo failed(UUID recipeId) throws RecipeInfoException {
        return repository.failed(recipeId, clock);
    }

    /**
     * 레시피 차단 처리
     *
     * @param recipeId 레시피 ID
     * @throws RecipeInfoException 레시피를 찾을 수 없을 때
     */
    public void block(UUID recipeId) throws RecipeInfoException {
        repository.block(recipeId, clock);
    }

    /**
     * 레시피 존재 여부 확인
     *
     * @param recipeId 레시피 ID
     * @return 존재 여부
     */
    public boolean exists(UUID recipeId) {
        return repository.exists(recipeId);
    }

    /**
     * 레시피 정보 조회
     *
     * @param recipeId 레시피 ID
     * @return 레시피 정보 엔티티
     * @throws RecipeInfoException 레시피를 찾을 수 없을 때
     */
    public RecipeInfo get(UUID recipeId) throws RecipeInfoException {
        return repository.get(recipeId);
    }

    /**
     * 요리 종류별 레시피 목록 조회 (커서 기반 페이징)
     *
     * @param type 요리 종류
     * @param cursor 페이징 커서
     * @return 요리 종류별 레시피 커서 페이지
     * @throws CursorException 커서 처리 중 예외 발생 시
     */
    public CursorPage<RecipeInfo> getCuisines(RecipeCuisineType type, String cursor) throws CursorException {
        String tag = i18nTranslator.translate(type.messageKey());
        boolean first = (cursor == null || cursor.isBlank());
        return first ? repository.cusineFirst(tag) : repository.cuisineKeyset(tag, cursor);
    }
}
