package io.yggdrasil.labs.mealmate.adapter.web.ai;

import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.cola.dto.SingleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.yggdrasil.labs.mealmate.app.mealplan.application.AiMealPlanAppService;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.AiMealPlanGenerateCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.AiMealPlanResultCO;
import lombok.RequiredArgsConstructor;

/**
 * AI 饮食计划生成 Controller。
 *
 * <p>提供基于 LLM 的周计划智能生成能力，LLM 不可用时自动 fallback 到规则引擎。
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai/meal-plans")
@Tag(name = "AI MealPlan", description = "AI-powered meal plan generation APIs.")
public class AiMealPlanController {

    private final AiMealPlanAppService aiMealPlanAppService;

    /** AI 生成一周三餐计划。接受家庭 ID、周起始日期和可选用户提示。 */
    @PostMapping("/generate")
    @Operation(
            summary = "AI generate weekly plan",
            description =
                    "Generates a weekly meal plan using LLM with automatic fallback to rule"
                            + " engine.")
    public SingleResponse<AiMealPlanResultCO> generate(
            @Valid @RequestBody AiMealPlanGenerateCmd cmd) {
        return SingleResponse.of(aiMealPlanAppService.generate(cmd));
    }
}
