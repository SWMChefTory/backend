package com.cheftory.api.recipe.content.youtubemeta;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.youtubemeta.client.YoutubeMetaClient;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeUri;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeVideoInfo;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaException;
import com.cheftory.api.recipe.content.youtubemeta.repository.RecipeYoutubeMetaRepository;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 레시피-YouTube 메타 도메인 서비스.
 *
 * <p>`recipeId` 기준 메타 조회/생성, URL에서 `videoId` 추출, 외부 YouTube 메타 조회를 담당합니다.
 * 생성 파이프라인에서는 메타 존재 여부를 먼저 확인해 외부 호출 비용을 줄이는 용도로 사용됩니다.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeYoutubeMetaService {

    private final RecipeYoutubeMetaRepository repository;
    private final YoutubeMetaClient client;
    private final Clock clock;

    /**
     * 유튜브 메타 정보 생성
     *
     * @param youtubeVideoInfo 유튜브 비디오 정보
     * @param recipeId 레시피 ID
     * @throws YoutubeMetaException 저장/도메인 검증 실패 시
     */
    public void create(YoutubeVideoInfo youtubeVideoInfo, UUID recipeId) throws YoutubeMetaException {
        RecipeYoutubeMeta youtubeMeta = RecipeYoutubeMeta.create(youtubeVideoInfo, recipeId, clock);
        repository.create(youtubeMeta);
    }

    /**
     * 지정한 레시피에 연결된 YouTube 메타가 이미 존재하는지 확인합니다.
     */
    public boolean exists(UUID recipeId) {
        return repository.exists(recipeId);
    }

    /**
     * 외부 API를 통해 유튜브 비디오 정보 조회
     *
     * @param videoId 유튜브 비디오 ID
     * @return 유튜브 비디오 정보
     * @throws YoutubeMetaException API 호출 실패 시
     */
    public YoutubeVideoInfo getVideoInfo(String videoId) throws YoutubeMetaException {
        return client.fetch(videoId);
    }

    /**
     * 레시피 ID로 유튜브 메타 정보 조회
     *
     * @param recipeId 레시피 ID
     * @return 유튜브 메타 정보
     * @throws YoutubeMetaException 메타 정보를 찾을 수 없을 때
     */
    public RecipeYoutubeMeta get(UUID recipeId) throws YoutubeMetaException {
        return repository.find(recipeId);
    }

    /**
     * 여러 레시피 ID로 유튜브 메타 정보 목록 조회
     *
     * @param recipeIds 레시피 ID 목록
     * @return 유튜브 메타 정보 목록
     */
    public List<RecipeYoutubeMeta> gets(List<UUID> recipeIds) {
        return repository.finds(recipeIds);
    }

    /**
     * 입력 URL을 정규화 파서로 해석해 videoId를 추출합니다.
     */
    public String getVideoId(URI uri) throws YoutubeMetaException {
        YoutubeUri youtubeUri = YoutubeUri.from(uri);
        return youtubeUri.getVideoId();
    }
}
