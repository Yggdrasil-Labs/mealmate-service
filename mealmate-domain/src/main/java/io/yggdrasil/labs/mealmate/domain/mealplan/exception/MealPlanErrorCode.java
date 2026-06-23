package io.yggdrasil.labs.mealmate.domain.mealplan.exception;

import io.yggdrasil.labs.mealmate.domain.common.exception.BizException;

/** 周餐计划领域错误码。 */
public enum MealPlanErrorCode implements BizException.ErrorCode {
    PLAN_NOT_FOUND("PLAN_NOT_FOUND", "计划不存在"),
    PLAN_WEEK_START_DATE_INVALID("PLAN_WEEK_START_DATE_INVALID", "周起始日期必须为周一"),
    PLAN_ALREADY_CONFIRMED("MEAL_PLAN_ALREADY_CONFIRMED", "计划已确认，无法编辑"),
    PLAN_ITEM_LAST_ONE("MEAL_PLAN_ITEM_LAST_ONE", "每餐至少保留一道菜品"),
    PLAN_FROZEN("MEAL_PLAN_FROZEN", "计划已锁定，无法调整"),
    ITEM_NOT_FOUND("ITEM_NOT_FOUND", "计划条目不存在"),
    CANDIDATE_RECIPES_INSUFFICIENT("CANDIDATE_RECIPES_INSUFFICIENT", "候选菜品不足，无法生成计划"),
    RECIPE_DUPLICATE_IN_WEEK("RECIPE_DUPLICATE_IN_WEEK", "该菜品本周已使用"),
    FAMILY_ID_REQUIRED("FAMILY_ID_REQUIRED", "缺少家庭标识"),
    ;

    private final String code;
    private final String message;

    MealPlanErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
