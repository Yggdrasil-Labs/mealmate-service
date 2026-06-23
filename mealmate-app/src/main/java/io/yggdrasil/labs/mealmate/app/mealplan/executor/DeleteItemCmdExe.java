package io.yggdrasil.labs.mealmate.app.mealplan.executor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.DeleteItemCmd;
import io.yggdrasil.labs.mealmate.domain.common.exception.BizException;
import io.yggdrasil.labs.mealmate.domain.mealplan.exception.MealPlanErrorCode;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.WeeklyMealPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.repo.WeeklyMealPlanRepository;
import lombok.RequiredArgsConstructor;

/** 删除计划条目命令执行器。 */
@Component
@RequiredArgsConstructor
public class DeleteItemCmdExe {

    private final WeeklyMealPlanRepository weeklyMealPlanRepository;

    @Transactional(rollbackFor = Exception.class)
    public void execute(DeleteItemCmd cmd) {
        MealPlanItem item =
                weeklyMealPlanRepository
                        .findItemById(cmd.getItemId())
                        .orElseThrow(() -> new BizException(MealPlanErrorCode.ITEM_NOT_FOUND));
        if (!cmd.getPlanId().equals(item.getPlanId())) {
            throw new BizException(MealPlanErrorCode.ITEM_NOT_FOUND);
        }
        assertPlanDraft(item.getPlanId());

        // 最后一项不可删除——委托聚合根统计同餐次条目数
        WeeklyMealPlan plan =
                weeklyMealPlanRepository
                        .findByIdWithItems(item.getPlanId())
                        .orElseThrow(() -> new BizException(MealPlanErrorCode.PLAN_NOT_FOUND));
        if (plan.countItemsInSlot(item.getMealDate(), item.getMealType()) <= 1) {
            throw new BizException(MealPlanErrorCode.PLAN_ITEM_LAST_ONE);
        }

        weeklyMealPlanRepository.deleteItem(cmd.getItemId());
    }

    private void assertPlanDraft(Long planId) {
        WeeklyMealPlan plan = assertPlanOwnership(planId);
        plan.assertDraft();
    }

    private WeeklyMealPlan assertPlanOwnership(Long planId) {
        return weeklyMealPlanRepository
                .findById(planId)
                .orElseThrow(() -> new BizException(MealPlanErrorCode.PLAN_NOT_FOUND));
    }
}
