package com.cheftory.api.recipeinfo.challenge;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.PocOnly;
import com.cheftory.api.recipeinfo.challenge.exception.RecipeChallengeErrorCode;
import com.cheftory.api.recipeinfo.challenge.exception.RecipeChallengeException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@PocOnly(until = "2025-12-31")
public class RecipeChallengeService {

  private final RecipeUserChallengeRepository recipeUserChallengeRepository;
  private final RecipeChallengeRepository recipeChallengeRepository;
  private final ChallengeRepository challengeRepository;
  private final RecipeUserChallengeCompletionRepository recipeUserChallengeCompletionRepository;
  private final Clock clock;

  public Challenge getUser(UUID userId) {
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

  public Page<RecipeCompleteChallenge> getChallengeRecipes(
      UUID userId, UUID challengeId, Integer page) {
    Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.ASC, "createdAt"));

    RecipeUserChallenge recipeUserChallenge =
        recipeUserChallengeRepository.findRecipeUserChallengeByUserIdAndChallengeId(
            userId, challengeId);

    if (recipeUserChallenge == null) {
      throw new RecipeChallengeException(RecipeChallengeErrorCode.RECIPE_CHALLENGE_NOT_FOUND);
    }

    Page<RecipeChallenge> recipeChallenges =
        recipeChallengeRepository.findAllByChallengeId(challengeId, pageable);

    List<RecipeChallenge> recipeChallengeList = recipeChallenges.getContent();

    List<UUID> recipeChallengeIds =
        recipeChallengeList.stream().map(RecipeChallenge::getId).toList();

    List<RecipeUserChallengeCompletion> completions =
        recipeChallengeIds.isEmpty()
            ? List.of()
            : recipeUserChallengeCompletionRepository
                .findByRecipeChallengeIdInAndRecipeUserChallengeId(
                    recipeChallengeIds, recipeUserChallenge.getId());

    Set<UUID> finishedRecipeChallengeIds =
        completions.stream()
            .map(RecipeUserChallengeCompletion::getRecipeChallengeId)
            .collect(Collectors.toSet());

    return recipeChallenges.map(
        recipeChallenge ->
            RecipeCompleteChallenge.of(
                recipeChallenge.getRecipeId(),
                finishedRecipeChallengeIds.contains(recipeChallenge.getId())));
  }
}
