package io.yggdrasil.labs.mealmate.app.mealplan.parser;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/** LLM 输出的中间结构，直接从 JSON 反序列化获得。 */
@Data
@NoArgsConstructor
public class AiMealPlanRawOutput {

    private List<DayPlan> days;

    @Data
    @NoArgsConstructor
    public static class DayPlan {
        private String date;
        private List<MealItem> breakfast;
        private List<MealItem> lunch;
        private List<MealItem> dinner;
        private String reasoning;
    }

    @Data
    @NoArgsConstructor
    public static class MealItem {
        private Long recipeId;
        private String recipeName;
    }
}
