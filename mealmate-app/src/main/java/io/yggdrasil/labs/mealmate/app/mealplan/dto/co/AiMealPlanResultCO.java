package io.yggdrasil.labs.mealmate.app.mealplan.dto.co;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** AI 周餐计划生成结果。包含计划概要、每日餐次安排、推理说明和是否回退标志。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiMealPlanResultCO {

    private Long planId;
    private String weekStartDate;
    private String weekEndDate;
    private String status;
    private String planSource;

    /** 按日期索引的每日三餐安排。key 为 "2026-07-07" 格式日期字符串。 */
    private Map<String, DayMealCO> dayMeals;

    /** AI 对每天配餐的推理说明。key 为日期字符串，value 为自然语言解释。 */
    private Map<String, String> reasoning;

    /** 是否因 AI 调用失败而回退到规则引擎生成。 */
    private boolean fallback;
}
