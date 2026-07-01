package io.yggdrasil.labs.mealmate.app.recipe.application;

import jakarta.validation.Valid;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.AiRecipeConfirmCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.AiRecipeParseChatCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.AiRecipeConfirmResultCO;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.AiRecipeParseResultCO;
import io.yggdrasil.labs.mealmate.app.recipe.executor.AiRecipeConfirmCmdExe;
import io.yggdrasil.labs.mealmate.app.recipe.executor.AiRecipeParseCmdExe;
import lombok.RequiredArgsConstructor;

/**
 * AI 菜品录入应用服务（facade）。
 *
 * <p>遵循 Controller → AppService → Executor 模式，触发 Cmd 校验并委托执行器处理。
 */
@Service
@Validated
@RequiredArgsConstructor
public class AiRecipeAppService {

    private final AiRecipeParseCmdExe aiRecipeParseCmdExe;
    private final AiRecipeConfirmCmdExe aiRecipeConfirmCmdExe;

    /** 对话式解析菜品 */
    public AiRecipeParseResultCO chat(@Valid AiRecipeParseChatCmd cmd) {
        return aiRecipeParseCmdExe.execute(cmd);
    }

    /** 确认入库 */
    public AiRecipeConfirmResultCO confirm(@Valid AiRecipeConfirmCmd cmd) {
        return aiRecipeConfirmCmdExe.execute(cmd);
    }
}
