package io.yggdrasil.labs.mealmate.app.mealplan.application;

import jakarta.validation.Valid;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.AiMealPlanGenerateCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.AiMealPlanResultCO;
import io.yggdrasil.labs.mealmate.app.mealplan.executor.AiMealPlanGenerateCmdExe;
import lombok.RequiredArgsConstructor;

/**
 * AI 周餐计划应用服务。
 *
 * <p>作为 facade 委托具体执行器完成 AI 配餐生成流程。 入口校验由 Jakarta Validation 驱动。
 */
@Service
@Validated
@RequiredArgsConstructor
public class AiMealPlanAppService {

    private final AiMealPlanGenerateCmdExe generateCmdExe;

    /**
     * AI 生成一周三餐计划。
     *
     * @param cmd 包含家庭 ID、周起始日期和可选用户提示
     * @return AI 配餐结果，包含每日安排、推理说明和是否回退标志
     */
    public AiMealPlanResultCO generate(@Valid AiMealPlanGenerateCmd cmd) {
        return generateCmdExe.execute(cmd);
    }
}
