package io.yggdrasil.labs.mealmate.domain.recipe.repo;

import java.util.List;
import java.util.Optional;

import io.yggdrasil.labs.mealmate.domain.recipe.model.NutritionFact;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeIngredient;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeQueryCriteria;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeStep;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeStatus;

/** 菜谱仓储接口。查询默认仅返回未逻辑删除的菜谱。 */
public interface RecipeRepository {

    Optional<Recipe> findById(Long recipeId);

    /** 批量查询菜谱（仅返回主体信息，不含 ingredients/steps/nutrition）。 */
    List<Recipe> findByIds(List<Long> ids);

    Optional<Recipe> findByName(String name);

    /** 分页查询菜谱列表。 */
    List<Recipe> page(RecipeQueryCriteria criteria);

    /** 统计满足条件的菜谱总数。 */
    int count(RecipeQueryCriteria criteria);

    List<Recipe> searchByKeyword(String keyword, Integer limit);

    Recipe save(Recipe recipe);

    void update(Recipe recipe);

    void updateIngredients(Long recipeId, List<RecipeIngredient> ingredients);

    void updateSteps(Long recipeId, List<RecipeStep> steps);

    void updateNutrition(Long recipeId, NutritionFact nutritionFact);

    void updateStatus(Long recipeId, RecipeStatus status);

    void logicalDeleteById(Long recipeId);
}
