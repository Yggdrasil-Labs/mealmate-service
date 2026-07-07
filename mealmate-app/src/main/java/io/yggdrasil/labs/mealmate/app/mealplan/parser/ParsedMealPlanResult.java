package io.yggdrasil.labs.mealmate.app.mealplan.parser;

import java.util.List;
import java.util.Map;

import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItem;
import lombok.Builder;
import lombok.Value;

/** 解析后的配餐结果，包含校验修正后的条目列表和每日 reasoning。 */
@Value
@Builder
public class ParsedMealPlanResult {

    /** 校验修正后的 35 条配餐条目（7 天 × 5 道/天）。 */
    List<MealPlanItem> items;

    /** 每日推理说明，key 为日期字符串（yyyy-MM-dd）。 */
    Map<String, String> reasoning;
}
