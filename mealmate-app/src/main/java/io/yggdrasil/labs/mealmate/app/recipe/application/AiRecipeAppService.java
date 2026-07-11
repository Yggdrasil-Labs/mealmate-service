package io.yggdrasil.labs.mealmate.app.recipe.application;

import java.util.function.Consumer;

import jakarta.validation.Valid;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.AiRecipeConfirmCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.AiRecipeParseChatCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.AiRecipeConfirmResultCO;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.AiRecipeParseResultCO;
import io.yggdrasil.labs.mealmate.app.recipe.executor.AiRecipeConfirmCmdExe;
import io.yggdrasil.labs.mealmate.app.recipe.executor.AiRecipeParseCmdExe;
import io.yggdrasil.labs.mealmate.app.recipe.executor.AiRecipeParseStreamCmdExe;
import lombok.RequiredArgsConstructor;

/**
 * AI 菜品录入应用服务（facade）。
 *
 * <p>遵循 Controller → AppService → Executor 模式，触发 Cmd 校验并委托执行器处理。 支持同步和流式两种调用模式。
 */
@Service
@Validated
@RequiredArgsConstructor
public class AiRecipeAppService {

    private final AiRecipeParseCmdExe aiRecipeParseCmdExe;
    private final AiRecipeParseStreamCmdExe aiRecipeParseStreamCmdExe;
    private final AiRecipeConfirmCmdExe aiRecipeConfirmCmdExe;

    /** 对话式解析菜品（同步） */
    public AiRecipeParseResultCO chat(@Valid AiRecipeParseChatCmd cmd) {
        return aiRecipeParseCmdExe.execute(cmd);
    }

    /**
     * 对话式解析菜品（流式）。
     *
     * @param cmd 对话命令
     * @param onChunk 每收到 LLM 增量文本时回调
     * @param onResult 流完成后回调完整解析结果
     * @param onError 异常时回调
     */
    public void chatStream(
            @Valid AiRecipeParseChatCmd cmd,
            Consumer<String> onChunk,
            Consumer<AiRecipeParseResultCO> onResult,
            Consumer<Exception> onError) {
        aiRecipeParseStreamCmdExe.execute(cmd, onChunk, onResult, onError);
    }

    /** 确认入库 */
    public AiRecipeConfirmResultCO confirm(@Valid AiRecipeConfirmCmd cmd) {
        return aiRecipeConfirmCmdExe.execute(cmd);
    }
}
