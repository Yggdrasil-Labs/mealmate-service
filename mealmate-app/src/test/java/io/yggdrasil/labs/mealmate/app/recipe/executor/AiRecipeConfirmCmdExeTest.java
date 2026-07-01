package io.yggdrasil.labs.mealmate.app.recipe.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.yggdrasil.labs.mealmate.app.recipe.convertor.RecipeParseDataConvertor;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.AiRecipeConfirmCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.CreateRecipeCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.AiRecipeConfirmResultCO;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.RecipeDetailCO;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiSession;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiSessionRepository;
import io.yggdrasil.labs.mealmate.domain.common.exception.BizException;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeParseCache;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeParsedData;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeParseStatus;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeParseCacheRepository;

@ExtendWith(MockitoExtension.class)
class AiRecipeConfirmCmdExeTest {

    @Mock AiSessionRepository sessionRepository;
    @Mock RecipeParseCacheRepository cacheRepository;
    @Mock RecipeParseDataConvertor convertor;
    @Mock CreateRecipeCmdExe createRecipeCmdExe;

    @InjectMocks AiRecipeConfirmCmdExe executor;

    @Test
    void confirm_createsRecipeAndReturnsId() {
        // Given
        RecipeParsedData parsedData = RecipeParsedData.builder().name("番茄炒蛋").build();
        AiRecipeConfirmCmd cmd = new AiRecipeConfirmCmd("session-1", parsedData);
        mockSession("session-1");
        mockCache("session-1", RecipeParseStatus.READY_TO_CONFIRM, null);
        CreateRecipeCmd createCmd = new CreateRecipeCmd();
        when(convertor.toCreateRecipeCmd(parsedData)).thenReturn(createCmd);
        RecipeDetailCO detail = new RecipeDetailCO();
        detail.setId(123L);
        when(createRecipeCmdExe.execute(any())).thenReturn(detail);

        // When
        AiRecipeConfirmResultCO result = executor.execute(cmd);

        // Then
        assertEquals(123L, result.getRecipeId());
        verify(cacheRepository).save(eq("session-1"), any());
        verify(cacheRepository).updateTtl(eq("session-1"), any());
    }

    @Test
    void duplicateConfirm_idempotentReturnsSameRecipeId() {
        // Given: cache 已 CONFIRMED
        RecipeParsedData parsedData = RecipeParsedData.builder().name("番茄炒蛋").build();
        AiRecipeConfirmCmd cmd = new AiRecipeConfirmCmd("session-1", parsedData);
        mockSession("session-1");
        mockCache("session-1", RecipeParseStatus.CONFIRMED, 456L);

        // When
        AiRecipeConfirmResultCO result = executor.execute(cmd);

        // Then: 幂等返回已有 recipeId，CreateRecipeCmdExe 未调用
        assertEquals(456L, result.getRecipeId());
        verify(createRecipeCmdExe, never()).execute(any());
    }

    @Test
    void incompleteRecipe_missingName_throwsBizException() {
        // Given: recipe 无 name
        RecipeParsedData parsedData = RecipeParsedData.builder().build(); // name=null
        AiRecipeConfirmCmd cmd = new AiRecipeConfirmCmd("session-1", parsedData);
        mockSession("session-1");
        mockCache("session-1", RecipeParseStatus.READY_TO_CONFIRM, null);
        when(convertor.toCreateRecipeCmd(parsedData))
                .thenThrow(new BizException("AI_RECIPE_INCOMPLETE", "菜品信息不完整"));

        // When/Then
        BizException ex = assertThrows(BizException.class, () -> executor.execute(cmd));
        assertEquals("AI_RECIPE_INCOMPLETE", ex.getErrCode());
    }

    @Test
    void sessionNotFound_throwsBizException() {
        // Given
        RecipeParsedData parsedData = RecipeParsedData.builder().name("test").build();
        AiRecipeConfirmCmd cmd = new AiRecipeConfirmCmd("nonexistent", parsedData);
        when(sessionRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When/Then
        BizException ex = assertThrows(BizException.class, () -> executor.execute(cmd));
        assertEquals("AI_SESSION_NOT_FOUND", ex.getErrCode());
    }

    @Test
    void cacheNotFound_throwsBizException() {
        // Given
        RecipeParsedData parsedData = RecipeParsedData.builder().name("test").build();
        AiRecipeConfirmCmd cmd = new AiRecipeConfirmCmd("session-1", parsedData);
        mockSession("session-1");
        when(cacheRepository.findBySessionId("session-1")).thenReturn(Optional.empty());

        // When/Then
        BizException ex = assertThrows(BizException.class, () -> executor.execute(cmd));
        assertEquals("AI_SESSION_NOT_FOUND", ex.getErrCode());
    }

    // --- Helpers ---

    private void mockSession(String sessionId) {
        AiSession session =
                AiSession.builder().sessionId(sessionId).createdAt(LocalDateTime.now()).build();
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
    }

    private void mockCache(String sessionId, RecipeParseStatus status, Long confirmedId) {
        RecipeParseCache cache =
                RecipeParseCache.builder()
                        .status(status)
                        .confirmedRecipeId(confirmedId)
                        .accumulatedParsed(RecipeParsedData.builder().name("番茄炒蛋").build())
                        .build();
        when(cacheRepository.findBySessionId(sessionId)).thenReturn(Optional.of(cache));
    }
}
