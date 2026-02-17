package com.cheftory.api.recipe.content.info.repository;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.cursor.*;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.entity.RecipeStatus;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoException;
import com.cheftory.api.recipe.dto.RecipeInfoVideoQuery;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * 레시피 기본 정보 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class RecipeInfoRepositoryImpl implements RecipeInfoRepository {

    private final RecipeInfoJpaRepository repository;
    private final CountIdCursorCodec countIdCursorCodec;

    /**
     * 레시피 ID로 정보 조회
     *
     * @param recipeId 레시피 ID
     * @return 레시피 정보 엔티티
     * @throws RecipeInfoException 레시피를 찾을 수 없을 때
     */
    @Override
    public RecipeInfo get(UUID recipeId) throws RecipeInfoException {

        return repository
                .findById(recipeId)
                .orElseThrow(() -> new RecipeInfoException(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND));
    }

    /**
     * 레시피 조회수 증가
     *
     * @param recipeId 레시피 ID
     */
    @Override
    public void increaseCount(UUID recipeId) {
        repository.increaseCount(recipeId);
    }

    /**
     * 레시피 정보 저장
     *
     * @param recipeInfo 저장할 레시피 정보 엔티티
     * @return 저장된 레시피 정보 엔티티
     */
    @Override
    public RecipeInfo create(RecipeInfo recipeInfo) {
        repository.save(recipeInfo);
        return recipeInfo;
    }

    /**
     * 진행 중인 레시피 목록 조회
     *
     * @param recipeIds 레시피 ID 목록
     * @return 진행 중인 레시피 정보 목록
     */
    @Override
    public List<RecipeInfo> getProgressRecipes(List<UUID> recipeIds) {
        return repository.findRecipesByIdInAndRecipeStatus(recipeIds, RecipeStatus.IN_PROGRESS).stream()
                .toList();
    }

    /**
     * 성공 상태의 레시피 정보 목록 조회
     *
     * @param recipeIds 레시피 ID 목록
     * @return 레시피 정보 목록
     */
    @Override
    public List<RecipeInfo> gets(List<UUID> recipeIds) {
        return repository.findRecipesByIdInAndRecipeStatus(recipeIds, RecipeStatus.SUCCESS).stream()
                .toList();
    }

    /**
     * 인기 레시피 첫 페이지 조회
     *
     * @param videoQuery 비디오 쿼리 조건
     * @return 인기 레시피 커서 페이지
     */
    @Override
    public CursorPage<RecipeInfo> popularFirst(RecipeInfoVideoQuery videoQuery) {

        Pageable pageable = CursorPageable.firstPage();
        Pageable probe = CursorPageable.probe(pageable);

        List<RecipeInfo> infos =
                switch (videoQuery) {
                    case ALL -> repository.findPopularFirst(RecipeStatus.SUCCESS, probe);
                    case NORMAL, SHORTS -> repository.findPopularByVideoTypeFirst(
                            RecipeStatus.SUCCESS, videoQuery.name(), probe);
                };

        return CursorPages.of(
                infos,
                pageable.getPageSize(),
                r -> countIdCursorCodec.encode(new CountIdCursor(r.getViewCount(), r.getId())));
    }

    /**
     * 인기 레시피 다음 페이지 조회 (커서 기반)
     *
     * @param videoQuery 비디오 쿼리 조건
     * @param cursor 페이징 커서
     * @return 인기 레시피 커서 페이지
     * @throws CursorException 커서 처리 중 예외 발생 시
     */
    @Override
    public CursorPage<RecipeInfo> popularKeyset(RecipeInfoVideoQuery videoQuery, String cursor) throws CursorException {

        Pageable pageable = CursorPageable.firstPage();
        Pageable probe = CursorPageable.probe(pageable);

        CountIdCursor idCursor = countIdCursorCodec.decode(cursor);

        List<RecipeInfo> recipeInfos =
                switch (videoQuery) {
                    case ALL -> repository.findPopularKeyset(
                            RecipeStatus.SUCCESS, idCursor.lastCount(), idCursor.lastId(), probe);
                    case NORMAL, SHORTS -> repository.findPopularByVideoTypeKeyset(
                            RecipeStatus.SUCCESS, videoQuery.name(), idCursor.lastCount(), idCursor.lastId(), probe);
                };
        return toCursorPage(recipeInfos, probe);
    }

    /**
     * 요리 종류별 레시피 첫 페이지 조회
     *
     * @param tag 요리 종류 태그
     * @return 요리 종류별 레시피 커서 페이지
     */
    @Override
    public CursorPage<RecipeInfo> cusineFirst(String tag) {

        Pageable pageable = CursorPageable.firstPage();
        Pageable probe = CursorPageable.probe(pageable);
        List<RecipeInfo> infos = repository.findCuisineFirst(tag, RecipeStatus.SUCCESS, probe);

        return toCursorPage(infos, probe);
    }

    /**
     * 요리 종류별 레시피 다음 페이지 조회 (커서 기반)
     *
     * @param tag 요리 종류 태그
     * @param cursor 페이징 커서
     * @return 요리 종류별 레시피 커서 페이지
     * @throws CursorException 커서 처리 중 예외 발생 시
     */
    @Override
    public CursorPage<RecipeInfo> cuisineKeyset(String tag, String cursor) throws CursorException {
        Pageable pageable = CursorPageable.firstPage();
        Pageable probe = CursorPageable.probe(pageable);
        CountIdCursor idCursor = countIdCursorCodec.decode(cursor);
        List<RecipeInfo> infos =
                repository.findCuisineKeyset(tag, RecipeStatus.SUCCESS, idCursor.lastCount(), idCursor.lastId(), probe);

        return toCursorPage(infos, probe);
    }

    /**
     * 레시피 목록을 커서 페이지로 변환
     *
     * @param infos 레시피 정보 목록
     * @param pageable 페이징 정보
     * @return 커서 페이지
     */
    private CursorPage<RecipeInfo> toCursorPage(List<RecipeInfo> infos, Pageable pageable) {

        return CursorPages.of(
                infos,
                pageable.getPageSize(),
                r -> countIdCursorCodec.encode(new CountIdCursor(r.getViewCount(), r.getId())));
    }

    /**
     * 레시피 상태를 실패로 변경
     *
     * @param recipeId 레시피 ID
     * @param clock 현재 시간 제공 객체
     * @return 업데이트된 레시피 정보 엔티티
     * @throws RecipeInfoException 레시피를 찾을 수 없을 때
     */
    @Override
    public RecipeInfo failed(UUID recipeId, Clock clock) throws RecipeInfoException {
        RecipeInfo recipeInfo = repository
                .findById(recipeId)
                .orElseThrow(() -> new RecipeInfoException(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND));
        recipeInfo.failed(clock);
        return repository.save(recipeInfo);
    }

    /**
     * 레시피 차단 처리
     *
     * @param recipeId 레시피 ID
     * @param clock 현재 시간 제공 객체
     * @throws RecipeInfoException 레시피를 찾을 수 없을 때
     */
    @Override
    public void block(UUID recipeId, Clock clock) throws RecipeInfoException {
        RecipeInfo recipeInfo = repository
                .findById(recipeId)
                .orElseThrow(() -> new RecipeInfoException(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND));
        recipeInfo.block(clock);
        repository.save(recipeInfo);
    }

    /**
     * 레시피 상태를 성공으로 변경
     *
     * @param recipeId 레시피 ID
     * @param clock 현재 시간 제공 객체
     * @return 업데이트된 레시피 정보 엔티티
     * @throws RecipeInfoException 레시피를 찾을 수 없을 때
     */
    @Override
    public RecipeInfo success(UUID recipeId, Clock clock) throws RecipeInfoException {
        RecipeInfo recipeInfo = repository
                .findById(recipeId)
                .orElseThrow(() -> new RecipeInfoException(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND));
        recipeInfo.success(clock);
        return repository.save(recipeInfo);
    }

    /**
     * 레시피 존재 여부 확인
     *
     * @param recipeId 레시피 ID
     * @return 존재 여부
     */
    @Override
    public boolean exists(UUID recipeId) {
        return repository.existsById(recipeId);
    }

    // ── 공개 레시피 API용 구현 ──

    @Override
    public java.util.Optional<RecipeInfo> findByIdPublic(UUID id) {
        return repository.findByIdAndIsPublicTrueAndRecipeStatus(id, RecipeStatus.SUCCESS);
    }

    @Override
    public CursorPage<RecipeInfo> publicFirst() {
        Pageable pageable = CursorPageable.firstPage();
        Pageable probe = CursorPageable.probe(pageable);
        List<RecipeInfo> infos = repository.findPublicFirst(RecipeStatus.SUCCESS, probe);
        return toCursorPage(infos, probe);
    }

    @Override
    public CursorPage<RecipeInfo> publicKeyset(String cursor) throws CursorException {
        Pageable pageable = CursorPageable.firstPage();
        Pageable probe = CursorPageable.probe(pageable);
        CountIdCursor idCursor = countIdCursorCodec.decode(cursor);
        List<RecipeInfo> infos =
                repository.findPublicKeyset(RecipeStatus.SUCCESS, idCursor.lastCount(), idCursor.lastId(), probe);
        return toCursorPage(infos, probe);
    }

    @Override
    public CursorPage<RecipeInfo> publicCuisineFirst(String tag) {
        Pageable pageable = CursorPageable.firstPage();
        Pageable probe = CursorPageable.probe(pageable);
        List<RecipeInfo> infos = repository.findPublicCuisineFirst(tag, RecipeStatus.SUCCESS, probe);
        return toCursorPage(infos, probe);
    }

    @Override
    public CursorPage<RecipeInfo> publicCuisineKeyset(String tag, String cursor) throws CursorException {
        Pageable pageable = CursorPageable.firstPage();
        Pageable probe = CursorPageable.probe(pageable);
        CountIdCursor idCursor = countIdCursorCodec.decode(cursor);
        List<RecipeInfo> infos = repository.findPublicCuisineKeyset(
                tag, RecipeStatus.SUCCESS, idCursor.lastCount(), idCursor.lastId(), probe);
        return toCursorPage(infos, probe);
    }

    @Override
    public List<RecipeInfo> findAllPublicForSitemap(int page, int size) {
        return repository.findAllPublic(RecipeStatus.SUCCESS, org.springframework.data.domain.PageRequest.of(page, size));
    }

    @Override
    public long countPublic() {
        return repository.countPublic(RecipeStatus.SUCCESS);
    }
}
