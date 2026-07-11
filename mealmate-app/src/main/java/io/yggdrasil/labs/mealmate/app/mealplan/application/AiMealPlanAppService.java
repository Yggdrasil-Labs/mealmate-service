package io.yggdrasil.labs.mealmate.app.mealplan.application;

import java.util.function.Consumer;

import jakarta.validation.Valid;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.AiMealPlanGenerateCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.AiMealPlanResultCO;
import io.yggdrasil.labs.mealmate.app.mealplan.executor.AiMealPlanGenerateCmdExe;
import io.yggdrasil.labs.mealmate.app.mealplan.executor.AiMealPlanGenerateStreamCmdExe;
import lombok.RequiredArgsConstructor;

/**
 * AI 周餐计划应用服务。
 *
 * <p>作为 facade 委托具体执行器完成 AI 配餐生成流程。 入口校验由 Jakarta Validation 驱动。 支持同步和流式两种调用模式。
 */
@Service
@Validated
@RequiredArgsConstructor
public class AiMealPlanAppService {

    private final AiMealPlanGenerateCmdExe generateCmdExe;
    private final AiMealPlanGenerateStreamCmdExe generateStreamCmdExe;

    /**
     * AI 生成一周三餐计划（同步）。
     *
     * @param cmd 包含家庭 ID、周起始日期和可选用户提示
     * @return AI 配餐结果，包含每日安排、推理说明和是否回退标志
     */
    public AiMealPlanResultCO generate(@Valid AiMealPlanGenerateCmd cmd) {
        return generateCmdExe.execute(cmd);
    }

    /**
     * AI 生成一周三餐计划（流式）。
     *
     * @param cmd 生成命令
     * @param onChunk 每收到 LLM 增量文本时回调
     * @param onResult 流完成或 fallback 完成后回调最终结果
     * @param onError 不可恢复异常时回调
     */
    public void generateStream(
            @Valid AiMealPlanGenerateCmd cmd,
            Consumer<String> onChunk,
            Consumer<AiMealPlanResultCO> onResult,
            Consumer<Exception> onError) {
        generateStreamCmdExe.execute(cmd, onChunk, onResult, onError);
    }
}
