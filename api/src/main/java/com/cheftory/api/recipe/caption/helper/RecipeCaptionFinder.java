package com.cheftory.api.recipe.caption.helper;

import com.cheftory.api.recipe.caption.CaptionNotFoundException;
import com.cheftory.api.recipe.caption.entity.Segment;
import com.cheftory.api.recipe.caption.entity.RecipeCaption;
import com.cheftory.api.recipe.caption.helper.repository.RecipeCaptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipeCaptionFinder {
    private final RecipeCaptionRepository recipeCaptionRepository;

    public RecipeCaption findById(UUID recipeCaptionId) {
        return recipeCaptionRepository
                .findById(recipeCaptionId)
                .orElseThrow(() -> new CaptionNotFoundException("id에 해당하는 caption이 존재하지 않습니다."));
    }


    public List<Segment> findSegmentsById(UUID captionId) {
        return findById(captionId)
                .getSegments();
    }
}
