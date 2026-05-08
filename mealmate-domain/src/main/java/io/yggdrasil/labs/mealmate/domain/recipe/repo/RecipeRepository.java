package io.yggdrasil.labs.mealmate.domain.recipe.repo;

import java.util.List;
import java.util.Optional;

import io.yggdrasil.labs.mealmate.domain.recipe.model.NutritionFact;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeIngredient;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeStep;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.CrowdTag;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.DifficultyLevel;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeStatus;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeType;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.SeasonTag;

/** 菜谱仓储接口。查询默认仅返回未逻辑删除的菜谱。 */
public interface RecipeRepository {

    /** 按主键查询菜谱详情。 */
    Optional<Recipe> findById(Long recipeId);

    /** 按名称查询菜谱，名称需在有效数据范围内唯一。 */
    Optional<Recipe> findByName(String name);

    /**
     * 分页查询菜谱列表。
     *
     * <p>实现需支持关键字、类型、季节、人群、宝宝友好、减脂友好、难度和最大烹饪时长筛选。
     */
    List<Recipe> page(
            String keyword,
            RecipeType recipeType,
            SeasonTag seasonTag,
            CrowdTag crowdTag,
            Boolean babyFriendly,
            Boolean weightLossFriendly,
            DifficultyLevel difficultyLevel,
            Integer maxCookingTime,
            Integer pageNum,
            Integer pageSize);

    /** 统计分页查询条件下的菜谱总数。 */
    int count(
            String keyword,
            RecipeType recipeType,
            SeasonTag seasonTag,
            CrowdTag crowdTag,
            Boolean babyFriendly,
            Boolean weightLossFriendly,
            DifficultyLevel difficultyLevel,
            Integer maxCookingTime);

    /** 按关键字模糊搜索菜名。 */
    List<Recipe> searchByKeyword(String keyword, Integer limit);

    /** 保存新菜谱并返回带持久化结果的实体。 */
    Recipe save(Recipe recipe);

    /** 更新菜谱基础信息。 */
    void update(Recipe recipe);

    /** 全量更新菜谱食材。 */
    void updateIngredients(Long recipeId, List<RecipeIngredient> ingredients);

    /** 全量更新菜谱步骤。 */
    void updateSteps(Long recipeId, List<RecipeStep> steps);

    /** 更新菜谱营养信息。 */
    void updateNutrition(Long recipeId, NutritionFact nutritionFact);

    /** 更新菜谱状态。 */
    void updateStatus(Long recipeId, RecipeStatus status);

    /** 对菜谱执行逻辑删除。 */
    void logicalDeleteById(Long recipeId);
}
