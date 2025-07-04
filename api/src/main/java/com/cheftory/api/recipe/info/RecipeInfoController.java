package com.cheftory.api.recipe.info;

import com.cheftory.api.recipe.info.dto.RecipeInfoFindResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/recipeinfos")
@RequiredArgsConstructor
public class RecipeInfoController {
    private final FindRecipeInfoService findRecipeInfoService;
    @GetMapping("/{recipeInfoId}")
    public RecipeInfoFindResponse getRecipeInfo(
            @PathVariable("recipeInfoId") UUID recipeInfoId
            , @RequestParam(defaultValue = "false") Boolean status
            , @RequestParam(defaultValue = "false") Boolean videoDetail) {
        return findRecipeInfoService.getInfoContent(recipeInfoId,status,videoDetail);
    }
}
