package io.yggdrasil.labs.mealmate.app.recipe.executor;

import java.time.Duration;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.yggdrasil.labs.mealmate.app.recipe.convertor.RecipeParseDataConvertor;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.AiRecipeConfirmCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.CreateRecipeCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.AiRecipeConfirmResultCO;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.RecipeDetailCO;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiErrorCode;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiSessionRepository;
import io.yggdrasil.labs.mealmate.domain.common.exception.BizException;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeParseCache;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeParseStatus;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeSourceType;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeParseCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AI 菜品确认入库命令执行器。
 *
 * <p>编排：幂等检查 → RecipeParsedData 转 CreateRecipeCmd → 设置 sourceType=AI_GENERATED → 委托入库 → 更新 cache。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiRecipeConfirmCmdExe {

    private static final Duration CONFIRM_TTL = Duration.ofHours(24);

    private final AiSessionRepository sessionRepository;
    private final RecipeParseCacheRepository parseCacheRepository;
    private final RecipeParseDataConvertor convertor;
    private final CreateRecipeCmdExe createRecipeCmdExe;

    @Transactional(rollbackFor = Exception.class)
    public AiRecipeConfirmResultCO execute(AiRecipeConfirmCmd cmd) {
        // 1. 加载 session，验证存在
        sessionRepository
                .findById(cmd.getSessionId())
                .orElseThrow(() -> new BizException(AiErrorCode.AI_SESSION_NOT_FOUND));

        // 2. 加载 cache
        RecipeParseCache cache =
                parseCacheRepository
                        .findBySessionId(cmd.getSessionId())
                        .orElseThrow(() -> new BizException(AiErrorCode.AI_SESSION_NOT_FOUND));

        // 3. 幂等：已确认直接返回
        if (cache.getStatus() == RecipeParseStatus.CONFIRMED
                && cache.getConfirmedRecipeId() != null) {
            return new AiRecipeConfirmResultCO(cache.getConfirmedRecipeId());
        }

        // 4. 转换：RecipeParsedData → CreateRecipeCmd（校验必填字段）
        CreateRecipeCmd createCmd = convertor.toCreateRecipeCmd(cmd.getRecipe());

        // 5. 设置 sourceType
        createCmd.setSourceType(RecipeSourceType.AI_GENERATED);

        // 6. 委托入库
        RecipeDetailCO detail = createRecipeCmdExe.execute(createCmd);

        // 7. 更新 cache 为 CONFIRMED
        cache.setStatus(RecipeParseStatus.CONFIRMED);
        cache.setConfirmedRecipeId(detail.getId());
        parseCacheRepository.save(cmd.getSessionId(), cache);
        parseCacheRepository.updateTtl(cmd.getSessionId(), CONFIRM_TTL);

        return new AiRecipeConfirmResultCO(detail.getId());
    }
}
