package com.cheftory.api.recipe.creation.pipeline;

import java.util.UUID;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 레시피 생성 파이프라인 실행 컨텍스트.
 *
 * <p>하나의 비동기 생성 실행(`jobId`) 동안 단계들이 공유하는 입력/중간 산출물을 담습니다.
 * 초기에는 `recipeId`, `videoId`, `jobId`만 가지고 시작하고, 파이프라인 단계가 파일 정보/유튜브 메타를 채웁니다.</p>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RecipeCreationExecutionContext {
    private UUID recipeId;
    private String videoId;
    private @Nullable String fileUri;
    private @Nullable String mimeType;
    private @Nullable String title;
    private UUID jobId;

    /**
     * 최소 입력(recipeId/videoId/jobId)으로 컨텍스트를 생성합니다.
     */
    public static RecipeCreationExecutionContext of(UUID recipeId, String videoId, UUID jobId) {
        return new RecipeCreationExecutionContext(recipeId, videoId, null, null, null, jobId);
    }

    /**
     * 제목이 이미 확보된 경우의 컨텍스트 생성 팩토리입니다.
     */
    public static RecipeCreationExecutionContext of(UUID recipeId, String videoId, String title, UUID jobId) {
        return new RecipeCreationExecutionContext(recipeId, videoId, null, null, title, jobId);
    }

    /**
     * 캡션/파일 추출 단계 결과를 반영한 새 컨텍스트를 반환합니다.
     */
    public static RecipeCreationExecutionContext withFileInfo(
            RecipeCreationExecutionContext context, String fileUri, String mimeType) {
        return new RecipeCreationExecutionContext(
                context.recipeId, context.videoId, fileUri, mimeType, context.title, context.jobId);
    }

    /**
     * YouTube 메타 로딩 단계 결과(videoId/title)를 반영한 새 컨텍스트를 반환합니다.
     */
    public static RecipeCreationExecutionContext withYoutubeMeta(
            RecipeCreationExecutionContext context, String videoId, String title) {
        return new RecipeCreationExecutionContext(
                context.recipeId, videoId, context.fileUri, context.mimeType, title, context.jobId);
    }
}
