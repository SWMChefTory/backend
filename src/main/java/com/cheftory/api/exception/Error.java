package com.cheftory.api.exception;

import com.cheftory.api._common.cursor.CursorErrorCode;
import com.cheftory.api.affiliate.coupang.exception.CoupangErrorCode;
import com.cheftory.api.auth.exception.AuthErrorCode;
import com.cheftory.api.auth.verifier.exception.VerificationErrorCode;
import com.cheftory.api.credit.exception.CreditErrorCode;
import com.cheftory.api.ranking.candidate.RankingCandidateErrorCode;
import com.cheftory.api.ranking.personalization.RankingPersonalizationErrorCode;
import com.cheftory.api.recipe.bookmark.exception.RecipeBookmarkErrorCode;
import com.cheftory.api.recipe.category.exception.RecipeCategoryErrorCode;
import com.cheftory.api.recipe.challenge.exception.RecipeChallengeErrorCode;
import com.cheftory.api.recipe.content.briefing.exception.RecipeBriefingErrorCode;
import com.cheftory.api.recipe.content.detailMeta.exception.RecipeDetailMetaErrorCode;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipe.content.step.exception.RecipeStepErrorCode;
import com.cheftory.api.recipe.content.verify.exception.RecipeVerifyErrorCode;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipe.creation.identify.exception.RecipeIdentifyErrorCode;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.rank.exception.RecipeRankErrorCode;
import com.cheftory.api.recipe.report.exception.RecipeReportErrorCode;
import com.cheftory.api.recipe.search.exception.RecipeSearchErrorCode;
import com.cheftory.api.search.exception.SearchErrorCode;
import com.cheftory.api.user.exception.UserErrorCode;
import com.cheftory.api.user.share.exception.UserShareErrorCode;
import com.cheftory.api.voicecommand.exception.VoiceCommandErrorCode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 에러 코드를 정의하는 인터페이스.
 *
 * <p>모든 에러는 이 인터페이스를 구현해야 합니다.</p>
 */
public interface Error {
    List<Class<? extends Enum<?>>> ERROR_ENUMS = List.of(
            GlobalErrorCode.class,
            CoupangErrorCode.class,
            SearchErrorCode.class,
            UserShareErrorCode.class,
            VoiceCommandErrorCode.class,
            CreditErrorCode.class,
            UserErrorCode.class,
            RecipeErrorCode.class,
            CursorErrorCode.class,
            RankingCandidateErrorCode.class,
            RecipeRankErrorCode.class,
            AuthErrorCode.class,
            RankingPersonalizationErrorCode.class,
            YoutubeMetaErrorCode.class,
            RecipeVerifyErrorCode.class,
            RecipeBriefingErrorCode.class,
            VerificationErrorCode.class,
            RecipeCategoryErrorCode.class,
            RecipeDetailMetaErrorCode.class,
            RecipeChallengeErrorCode.class,
            RecipeReportErrorCode.class,
            RecipeStepErrorCode.class,
            RecipeInfoErrorCode.class,
            RecipeBookmarkErrorCode.class,
            RecipeIdentifyErrorCode.class,
            RecipeSearchErrorCode.class);

    Map<String, Error> ERROR_BY_CODE = buildErrorByCode();

    /**
     * 에러 코드를 반환합니다.
     *
     * @return 에러 코드
     */
    String getErrorCode();

    /**
     * 에러 메시지를 반환합니다.
     *
     * @return 에러 메시지
     */
    String getMessage();

    /**
     * 에러의 의미 분류를 반환합니다.
     *
     * @return 에러 타입
     */
    ErrorType getType();

    /**
     * 에러 코드 문자열로부터 Error 인스턴스를 찾습니다.
     *
     * @param errorCode 에러 코드 문자열
     * @return Error 인스턴스 (찾지 못하면 UNKNOWN_ERROR)
     */
    static Error resolveErrorCode(String errorCode) {
        if (errorCode == null || errorCode.isBlank()) return GlobalErrorCode.UNKNOWN_ERROR;

        Error resolvedByCode = ERROR_BY_CODE.get(errorCode);
        if (resolvedByCode != null) return resolvedByCode;

        return GlobalErrorCode.UNKNOWN_ERROR;
    }

    private static Map<String, Error> buildErrorByCode() {
        Map<String, Error> byCode = new HashMap<>();
        Set<String> duplicatedCodes = new HashSet<>();

        for (Class<? extends Enum<?>> enumClass : ERROR_ENUMS) {
            for (Enum<?> e : enumClass.getEnumConstants()) {
                if (!(e instanceof Error ec)) continue;
                String key = ec.getErrorCode();
                if (duplicatedCodes.contains(key)) continue;

                Error previous = byCode.putIfAbsent(key, ec);
                if (previous != null) {
                    byCode.remove(key);
                    duplicatedCodes.add(key);
                }
            }
        }

        return Map.copyOf(byCode);
    }
}
