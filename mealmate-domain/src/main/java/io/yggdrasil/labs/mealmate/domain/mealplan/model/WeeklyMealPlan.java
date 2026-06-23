package io.yggdrasil.labs.mealmate.domain.mealplan.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import io.yggdrasil.labs.mealmate.domain.common.exception.BizException;
import io.yggdrasil.labs.mealmate.domain.mealplan.exception.MealPlanErrorCode;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.MealType;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PlanSource;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PlanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 周餐计划聚合根。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyMealPlan {

    private Long id;
    private Long familyId;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private PlanStatus status;
    private PlanSource planSource;
    private String ruleSnapshotJson;
    private String remark;
    private LocalDateTime generatedTime;
    private List<MealPlanItem> items;

    // ─── 领域行为 ───

    /** 断言计划处于可编辑状态（DRAFT）。非 DRAFT 状态抵绝编辑操作。 */
    public void assertDraft() {
        if (this.status != PlanStatus.DRAFT) {
            throw new BizException(MealPlanErrorCode.PLAN_ALREADY_CONFIRMED);
        }
    }

    /** 确认计划，状态从 DRAFT 转为 CONFIRMED。已确认计划不可重复确认。 */
    public void confirm() {
        assertDraft();
        this.status = PlanStatus.CONFIRMED;
    }

    /** 统计指定日期和餐次的条目数量。 */
    public long countItemsInSlot(LocalDate mealDate, MealType mealType) {
        if (items == null) {
            return 0;
        }
        return items.stream()
                .filter(i -> mealDate.equals(i.getMealDate()) && i.getMealType() == mealType)
                .count();
    }
}
