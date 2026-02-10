package com.cheftory.api.recipe.creation.identify.repository;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.creation.identify.entity.RecipeIdentify;
import com.cheftory.api.recipe.creation.identify.exception.RecipeIdentifyException;
import java.net.URI;

/**
 * 레시피 식별 Repository 인터페이스.
 *
 * <p>레시피 생성 중복 방지를 위해 URL을 관리합니다.</p>
 */
public interface RecipeIdentifyRepository {

    /**
     * 레시피 식별 정보 생성.
     *
     * @param recipeIdentify 생성할 레시피 식별 정보
     * @param clock 현재 시간 제공 객체
     * @return 생성된 레시피 식별 정보
     * @throws RecipeIdentifyException 이미 진행 중인 URL인 경우
     */
    RecipeIdentify create(RecipeIdentify recipeIdentify, Clock clock) throws RecipeIdentifyException;

    /**
     * 레시피 식별 정보 삭제.
     *
     * @param url 삭제할 식별 정보의 URL
     */
    void delete(URI url);
}
