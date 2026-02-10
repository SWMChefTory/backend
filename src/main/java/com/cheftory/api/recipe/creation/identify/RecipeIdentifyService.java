package com.cheftory.api.recipe.creation.identify;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.creation.identify.entity.RecipeIdentify;
import com.cheftory.api.recipe.creation.identify.exception.RecipeIdentifyException;
import com.cheftory.api.recipe.creation.identify.repository.RecipeIdentifyRepository;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 레시피 식별 서비스.
 *
 * <p>레시피 생성 중복 방지를 위해 URL을 관리합니다.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeIdentifyService {

    private final RecipeIdentifyRepository repository;
    private final Clock clock;

    /**
     * 레시피 식별 정보를 생성합니다.
     *
     * <p>동일한 URL로 레시피 생성이 중복되지 않도록 식별 정보를 저장합니다.</p>
     *
     * @param url 레시피 생성 대상 URL
     * @return 생성된 레시피 식별 정보
     * @throws RecipeIdentifyException 이미 진행 중인 URL인 경우
     */
    public RecipeIdentify create(URI url) throws RecipeIdentifyException {
        RecipeIdentify recipeIdentify = RecipeIdentify.create(url, clock);
        return repository.create(recipeIdentify, clock);
    }

    /**
     * 레시피 식별 정보를 삭제합니다.
     *
     * <p>레시피 생성 완료 후 식별 정보를 정리합니다.</p>
     *
     * @param url 삭제할 식별 정보의 URL
     */
    public void delete(URI url) {
        repository.delete(url);
    }
}
