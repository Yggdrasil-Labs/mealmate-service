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
import io.yggdrasil.labs.mealmate.app.recipe.application.AiRecipeAppService;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.AiRecipeConfirmCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.AiRecipeParseChatCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.AiRecipeConfirmResultCO;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.AiRecipeParseResultCO;
import lombok.RequiredArgsConstructor;

/**
 * AI 菜品录入 Controller。
 *
 * <p>暴露对话式解析和确认入库两个端点，遵循 Controller → AppService → Executor 模式。
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai/recipes")
@Tag(name = "AI Recipe", description = "AI-assisted recipe input APIs.")
public class AiRecipeController {

    private final AiRecipeAppService aiRecipeAppService;

    /** 对话式解析菜品。首次调用 sessionId 为 null，后续带上返回的 sessionId。 */
    @PostMapping("/chat")
    @Operation(summary = "AI 对话解析菜品", description = "通过自然语言描述菜品，AI 解析为结构化数据")
    public SingleResponse<AiRecipeParseResultCO> chat(
            @RequestBody @Valid AiRecipeParseChatCmd cmd) {
        return SingleResponse.of(aiRecipeAppService.chat(cmd));
    }

    /** 确认入库。用户可编辑 parsed 后提交。幂等：同一 sessionId 重复提交返回已有 recipeId。 */
    @PostMapping("/confirm")
    @Operation(summary = "确认 AI 解析菜品入库", description = "确认解析结果并入库")
    public SingleResponse<AiRecipeConfirmResultCO> confirm(
            @RequestBody @Valid AiRecipeConfirmCmd cmd) {
        return SingleResponse.of(aiRecipeAppService.confirm(cmd));
    }
}
