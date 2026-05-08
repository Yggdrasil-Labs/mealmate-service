package io.yggdrasil.labs.mealmate.adapter.web.recipe;

import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.convertor.RecipeWebConvertor;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.CreateRecipeRequest;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.RecipeDetailResponse;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.RecipePageRequest;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.RecipeResponse;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.RecipeSearchRequest;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.UpdateRecipeIngredientsRequest;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.UpdateRecipeNutritionRequest;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.UpdateRecipeRequest;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.UpdateRecipeStatusRequest;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.UpdateRecipeStepsRequest;
import io.yggdrasil.labs.mealmate.app.recipe.application.RecipeAppService;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
@Tag(name = "Recipe", description = "Recipe management APIs.")
public class RecipeController {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final RecipeAppService recipeAppService;
    private final RecipeWebConvertor recipeWebConvertor;

    @GetMapping
    @Operation(summary = "Page recipes", description = "Returns paged recipe summaries.")
    public PageResponse<RecipeResponse> pageRecipes(
            @Valid @ModelAttribute RecipePageRequest request) {
        var pageQry = recipeWebConvertor.toPageRecipeQry(request);
        var recipeList = recipeAppService.pageRecipe(pageQry);
        int totalCount = recipeAppService.countRecipe(pageQry);
        return PageResponse.of(
                recipeWebConvertor.toRecipeResponseList(recipeList),
                totalCount,
                normalizePageSize(request.getPageSize()),
                normalizePageNum(request.getPageNum()));
    }

    @GetMapping("/search")
    @Operation(summary = "Search recipes", description = "Returns lightweight recipe matches.")
    public MultiResponse<RecipeResponse> searchRecipes(
            @Valid @ModelAttribute RecipeSearchRequest request) {
        MultiResponse<RecipeResponse> response = MultiResponse.<RecipeResponse>buildSuccess();
        response.setData(
                recipeWebConvertor.toRecipeResponseList(
                        recipeAppService.searchRecipe(
                                recipeWebConvertor.toSearchRecipeQry(request))));
        return response;
    }

    @GetMapping("/{recipeId}")
    @Operation(summary = "Get recipe detail", description = "Returns detailed recipe information.")
    public SingleResponse<RecipeDetailResponse> getRecipeDetail(
            @Parameter(description = "Unique identifier of the recipe.") @PathVariable("recipeId")
                    Long recipeId) {
        SingleResponse<RecipeDetailResponse> response =
                SingleResponse.<RecipeDetailResponse>buildSuccess();
        response.setData(
                recipeWebConvertor.toRecipeDetailResponse(
                        recipeAppService.getRecipeDetail(
                                recipeWebConvertor.toGetRecipeDetailQry(recipeId))));
        return response;
    }

    @PostMapping
    @Operation(summary = "Create recipe", description = "Creates a new custom recipe.")
    public SingleResponse<RecipeDetailResponse> createRecipe(
            @Valid @RequestBody CreateRecipeRequest request) {
        SingleResponse<RecipeDetailResponse> response =
                SingleResponse.<RecipeDetailResponse>buildSuccess();
        response.setData(
                recipeWebConvertor.toRecipeDetailResponse(
                        recipeAppService.createRecipe(
                                recipeWebConvertor.toCreateRecipeCmd(request))));
        return response;
    }

    @PutMapping("/{recipeId}")
    @Operation(
            summary = "Update recipe basics",
            description = "Updates the basic fields of an existing recipe.")
    public Response updateRecipe(
            @Parameter(description = "Unique identifier of the recipe.") @PathVariable("recipeId")
                    Long recipeId,
            @Valid @RequestBody UpdateRecipeRequest request) {
        recipeAppService.updateRecipe(recipeWebConvertor.toUpdateRecipeCmd(recipeId, request));
        return Response.buildSuccess();
    }

    @PutMapping("/{recipeId}/ingredients")
    @Operation(
            summary = "Replace recipe ingredients",
            description = "Replaces the full ingredient list of a recipe.")
    public Response updateRecipeIngredients(
            @Parameter(description = "Unique identifier of the recipe.") @PathVariable("recipeId")
                    Long recipeId,
            @Valid @RequestBody UpdateRecipeIngredientsRequest request) {
        recipeAppService.updateRecipeIngredients(
                recipeWebConvertor.toUpdateRecipeIngredientsCmd(recipeId, request));
        return Response.buildSuccess();
    }

    @PutMapping("/{recipeId}/steps")
    @Operation(
            summary = "Replace recipe steps",
            description = "Replaces the full step list of a recipe.")
    public Response updateRecipeSteps(
            @Parameter(description = "Unique identifier of the recipe.") @PathVariable("recipeId")
                    Long recipeId,
            @Valid @RequestBody UpdateRecipeStepsRequest request) {
        recipeAppService.updateRecipeSteps(
                recipeWebConvertor.toUpdateRecipeStepsCmd(recipeId, request));
        return Response.buildSuccess();
    }

    @PutMapping("/{recipeId}/nutrition")
    @Operation(summary = "Update recipe nutrition", description = "Updates nutrition information.")
    public Response updateRecipeNutrition(
            @Parameter(description = "Unique identifier of the recipe.") @PathVariable("recipeId")
                    Long recipeId,
            @Valid @RequestBody UpdateRecipeNutritionRequest request) {
        recipeAppService.updateRecipeNutrition(
                recipeWebConvertor.toUpdateRecipeNutritionCmd(recipeId, request));
        return Response.buildSuccess();
    }

    @PutMapping("/{recipeId}/status")
    @Operation(summary = "Update recipe status", description = "Updates recipe lifecycle status.")
    public Response updateRecipeStatus(
            @Parameter(description = "Unique identifier of the recipe.") @PathVariable("recipeId")
                    Long recipeId,
            @Valid @RequestBody UpdateRecipeStatusRequest request) {
        recipeAppService.updateRecipeStatus(
                recipeWebConvertor.toUpdateRecipeStatusCmd(recipeId, request));
        return Response.buildSuccess();
    }

    @DeleteMapping("/{recipeId}")
    @Operation(summary = "Delete recipe", description = "Logically deletes a recipe.")
    public Response deleteRecipe(
            @Parameter(description = "Unique identifier of the recipe.") @PathVariable("recipeId")
                    Long recipeId) {
        recipeAppService.deleteRecipe(recipeWebConvertor.toDeleteRecipeCmd(recipeId));
        return Response.buildSuccess();
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum <= 0 ? DEFAULT_PAGE_NUM : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
    }
}
