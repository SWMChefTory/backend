package com.cheftory.api.recipe.content.info.repository;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.cursor.CursorException;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoException;
import com.cheftory.api.recipe.dto.RecipeInfoVideoQuery;
import java.util.List;
import java.util.UUID;

/**
 * 레시피 기본 정보 Repository 인터페이스
 */
public interface RecipeInfoRepository {
    /**
     * 레시피 ID로 정보 조회
     *
     * @param recipeId 레시피 ID
     * @return 레시피 정보 엔티티
     * @throws RecipeInfoException 레시피를 찾을 수 없을 때
     */
    RecipeInfo get(UUID recipeId) throws RecipeInfoException;

    /**
     * 레시피 조회수 증가
     *
     * @param recipeId 레시피 ID
     */
    void increaseCount(UUID recipeId);

    /**
     * 레시피 정보 저장
     *
     * @param recipeInfo 저장할 레시피 정보 엔티티
     * @return 저장된 레시피 정보 엔티티
     */
    RecipeInfo create(RecipeInfo recipeInfo);

    /**
     * 진행 중인 레시피 목록 조회
     *
     * @param recipeIds 레시피 ID 목록
     * @return 진행 중인 레시피 정보 목록
     */
    List<RecipeInfo> getProgressRecipes(List<UUID> recipeIds);

    /**
     * 레시피 정보 목록 조회
     *
     * @param recipeIds 레시피 ID 목록
     * @return 레시피 정보 목록
     */
    List<RecipeInfo> gets(List<UUID> recipeIds);

    /**
     * 인기 레시피 첫 페이지 조회
     *
     * @param videoQuery 비디오 쿼리 조건
     * @return 인기 레시피 커서 페이지
     */
    CursorPage<RecipeInfo> popularFirst(RecipeInfoVideoQuery videoQuery);

    /**
     * 인기 레시피 다음 페이지 조회 (커서 기반)
     *
     * @param videoQuery 비디오 쿼리 조건
     * @param cursor 페이징 커서
     * @return 인기 레시피 커서 페이지
     * @throws CursorException 커서 처리 중 예외 발생 시
     */
    CursorPage<RecipeInfo> popularKeyset(RecipeInfoVideoQuery videoQuery, String cursor) throws CursorException;

    /**
     * 요리 종류별 레시피 첫 페이지 조회
     *
     * @param tag 요리 종류 태그
     * @return 요리 종류별 레시피 커서 페이지
     */
    CursorPage<RecipeInfo> cusineFirst(String tag);

    /**
     * 요리 종류별 레시피 다음 페이지 조회 (커서 기반)
     *
     * @param tag 요리 종류 태그
     * @param cursor 페이징 커서
     * @return 요리 종류별 레시피 커서 페이지
     * @throws CursorException 커서 처리 중 예외 발생 시
     */
    CursorPage<RecipeInfo> cuisineKeyset(String tag, String cursor) throws CursorException;

    /**
     * 레시피 상태를 실패로 변경
     *
     * @param recipeId 레시피 ID
     * @param clock 현재 시간 제공 객체
     * @return 업데이트된 레시피 정보 엔티티
     * @throws RecipeInfoException 레시피를 찾을 수 없을 때
     */
    RecipeInfo failed(UUID recipeId, Clock clock) throws RecipeInfoException;

    /**
     * 레시피 차단 처리
     *
     * @param recipeId 레시피 ID
     * @param clock 현재 시간 제공 객체
     * @throws RecipeInfoException 레시피를 찾을 수 없을 때
     */
    void block(UUID recipeId, Clock clock) throws RecipeInfoException;

    /**
     * 레시피 상태를 성공으로 변경
     *
     * @param recipeId 레시피 ID
     * @param clock 현재 시간 제공 객체
     * @return 업데이트된 레시피 정보 엔티티
     * @throws RecipeInfoException 레시피를 찾을 수 없을 때
     */
    RecipeInfo success(UUID recipeId, Clock clock) throws RecipeInfoException;

    /**
     * 레시피 존재 여부 확인
     *
     * @param recipeId 레시피 ID
     * @return 존재 여부
     */
    boolean exists(UUID recipeId);
}
