package io.yggdrasil.labs.mealmate.domain.mealplan.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.yggdrasil.labs.mealmate.domain.common.exception.BizException;
import io.yggdrasil.labs.mealmate.domain.mealplan.exception.MealPlanErrorCode;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItem;

/** 周计划不重样规则领域服务：同 crowdType 同周内菜品不重复。 */
public class MealPlanRuleDomainService {

    /**
     * 校验同 crowdType 下新菜品是否与周内已有条目重复。
     *
     * @param items 当前周计划所有条目
     * @param currentItemId 正在修改的条目 ID（排除自身）
     * @param newRecipeId 欲替换的新菜品 ID
     */
    public void validateNoDuplicate(
            List<MealPlanItem> items, Long currentItemId, Long newRecipeId) {
        MealPlanItem current =
                items.stream()
                        .filter(i -> i.getId().equals(currentItemId))
                        .findFirst()
                        .orElseThrow(() -> new BizException(MealPlanErrorCode.ITEM_NOT_FOUND));

        boolean duplicate =
                items.stream()
                        .filter(i -> !i.getId().equals(currentItemId))
                        .filter(i -> i.getCrowdType() == current.getCrowdType())
                        .anyMatch(i -> i.getRecipeId().equals(newRecipeId));

        if (duplicate) {
            throw new BizException(MealPlanErrorCode.RECIPE_DUPLICATE_IN_WEEK);
        }
    }

    /** 提取周计划中所有已使用的菜品 ID 集合。 */
    public Set<Long> getUsedRecipeIds(List<MealPlanItem> items) {
        return items.stream().map(MealPlanItem::getRecipeId).collect(Collectors.toSet());
    }
}
