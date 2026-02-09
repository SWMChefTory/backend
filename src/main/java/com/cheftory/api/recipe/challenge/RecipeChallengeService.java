package com.cheftory.api.recipe.challenge;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.PocOnly;
import com.cheftory.api._common.cursor.*;
import com.cheftory.api.recipe.challenge.exception.RecipeChallengeErrorCode;
import com.cheftory.api.recipe.challenge.exception.RecipeChallengeException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@PocOnly(until = "2025-12-31")
public class RecipeChallengeService {

    private static final int CHALLENGE_PAGE_SIZE = 10;
    private static final Sort CHALLENGE_SORT =
            Sort.by(Sort.Direction.ASC, "createdAt").and(Sort.by(Sort.Direction.ASC, "id"));

    private final RecipeUserChallengeRepository recipeUserChallengeRepository;
    private final RecipeChallengeRepository recipeChallengeRepository;
    private final ChallengeRepository challengeRepository;
    private final RecipeUserChallengeCompletionRepository recipeUserChallengeCompletionRepository;
    private final Clock clock;
    private final ViewedAtCursorCodec viewedAtCursorCodec;

    public Challenge getUser(UUID userId) throws RecipeChallengeException {
        var now = clock.now();

        List<Challenge> challenges = challengeRepository.findOngoing(now);
        if (challenges.isEmpty()) {
            throw new RecipeChallengeException(RecipeChallengeErrorCode.RECIPE_CHALLENGE_NOT_FOUND);
        }

        var challengeMap = challenges.stream().collect(Collectors.toMap(Challenge::getId, c -> c));

        List<RecipeUserChallenge> recipeUserChallenges =
                recipeUserChallengeRepository.findRecipeUserChallengesByUserIdAndChallengeIdIn(
                        userId, challengeMap.keySet().stream().toList());

        if (recipeUserChallenges.isEmpty()) {
            throw new RecipeChallengeException(RecipeChallengeErrorCode.RECIPE_CHALLENGE_NOT_FOUND);
        }
        if (recipeUserChallenges.size() > 1) {
            log.error("참여한 챌린지가 2개 이상입니다: {}", userId);
        }

        UUID challengeId = recipeUserChallenges.getFirst().getChallengeId();

        return challengeMap.get(challengeId);
    }

    public CursorPage<RecipeCompleteChallenge> getChallengeRecipes(UUID userId, UUID challengeId, String cursor)
            throws RecipeChallengeException, CursorException {
        Pageable pageable = PageRequest.of(0, CHALLENGE_PAGE_SIZE, CHALLENGE_SORT);
        Pageable probe = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize() + 1, pageable.getSort());
        boolean first = (cursor == null || cursor.isBlank());

        RecipeUserChallenge recipeUserChallenge =
                recipeUserChallengeRepository.findRecipeUserChallengeByUserIdAndChallengeId(userId, challengeId);

        if (recipeUserChallenge == null) {
            throw new RecipeChallengeException(RecipeChallengeErrorCode.RECIPE_CHALLENGE_NOT_FOUND);
        }

        List<RecipeChallenge> recipeChallenges = first
                ? recipeChallengeRepository.findChallengeFirst(challengeId, probe)
                : keyset(challengeId, cursor, probe);

        CursorPage<RecipeChallenge> page = CursorPages.of(
                recipeChallenges,
                pageable.getPageSize(),
                recipeChallenge -> viewedAtCursorCodec.encode(
                        new ViewedAtCursor(recipeChallenge.getCreatedAt(), recipeChallenge.getId())));

        List<RecipeChallenge> recipeChallengeList = page.items();
        List<UUID> recipeChallengeIds =
                recipeChallengeList.stream().map(RecipeChallenge::getId).toList();

        List<RecipeUserChallengeCompletion> completions = recipeChallengeIds.isEmpty()
                ? List.of()
                : recipeUserChallengeCompletionRepository.findByRecipeChallengeIdInAndRecipeUserChallengeId(
                        recipeChallengeIds, recipeUserChallenge.getId());

        Set<UUID> finishedRecipeChallengeIds = completions.stream()
                .map(RecipeUserChallengeCompletion::getRecipeChallengeId)
                .collect(Collectors.toSet());

        List<RecipeCompleteChallenge> items = recipeChallengeList.stream()
                .map(recipeChallenge -> RecipeCompleteChallenge.of(
                        recipeChallenge.getRecipeId(), finishedRecipeChallengeIds.contains(recipeChallenge.getId())))
                .toList();

        return CursorPage.of(items, page.nextCursor());
    }

    private List<RecipeChallenge> keyset(UUID challengeId, String cursor, Pageable probe) throws CursorException {
        ViewedAtCursor p = viewedAtCursorCodec.decode(cursor);
        return recipeChallengeRepository.findChallengeKeyset(challengeId, p.lastViewedAt(), p.lastId(), probe);
    }
}
