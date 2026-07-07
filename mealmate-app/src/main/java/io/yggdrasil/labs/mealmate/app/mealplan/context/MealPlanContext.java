package io.yggdrasil.labs.mealmate.app.mealplan.context;

import java.util.List;
import java.util.Set;

import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** AI 配餐上下文聚合。汇总家庭画像、偏好摘要、候选菜品和忌口信息，供 Prompt 构建和 fallback 策略使用。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanContext {

    /** 家庭成员画像摘要（角色代称 + 年龄段 + 饮食目标），不含真实姓名。 */
    private String familySummary;

    /** 偏好汇总（忌口、过敏、口味偏好的自然语言描述）。 */
    private String preferenceSummary;

    /** 候选菜品目录文本（ID + 名称 + 标签），供 AI prompt 引用。 */
    private String recipeCatalog;

    /** 候选菜品 ID 列表，用于 AI 结果校验。 */
    private List<Long> candidateIds;

    /** 候选菜品对象列表，用于 fallback 策略和结果解析。 */
    private List<Recipe> candidateRecipes;

    /** 所有成员的忌口食材汇总。 */
    private Set<String> avoidIngredients;

    /** 所有成员的过敏食材汇总。 */
    private Set<String> allergyIngredients;
}
