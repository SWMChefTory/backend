package com.cheftory.api.recipe.dto;

import java.net.URI;
import java.util.UUID;

/**
 * 레시피 생성 타겟.
 *
 * <p>사용자 요청 또는 크롤러에 의한 레시피 생성을 구분합니다.</p>
 */
public sealed interface RecipeCreationTarget permits RecipeCreationTarget.User, RecipeCreationTarget.Crawler {

    /**
     * 레시피 비디오 URI.
     *
     * @return 비디오 URI
     */
    URI uri();

    /**
     * 사용자 요청에 의한 레시피 생성 타겟.
     *
     * @param uri 비디오 URI
     * @param userId 사용자 ID
     */
    record User(URI uri, UUID userId) implements RecipeCreationTarget {}

    /**
     * 크롤러에 의한 레시피 생성 타겟.
     *
     * @param uri 비디오 URI
     */
    record Crawler(URI uri) implements RecipeCreationTarget {}
}
