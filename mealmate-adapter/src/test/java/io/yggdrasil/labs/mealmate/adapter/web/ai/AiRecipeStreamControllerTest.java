package io.yggdrasil.labs.mealmate.adapter.web.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.yggdrasil.labs.mealmate.app.mealplan.application.AiMealPlanAppService;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.AiMealPlanGenerateCmd;
import io.yggdrasil.labs.mealmate.app.recipe.application.AiRecipeAppService;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.AiRecipeParseChatCmd;

/**
 * AiRecipeStreamController 和 AiMealPlanStreamController 单元测试。
 *
 * <p>验证 Controller 返回 SseEmitter 且正确提交异步任务到 executor。 不验证实际 SSE 事件发送（那属于集成测试范畴）。
 */
@ExtendWith(MockitoExtension.class)
class AiRecipeStreamControllerTest {

    @Mock private AiRecipeAppService aiRecipeAppService;
    @Mock private AiMealPlanAppService aiMealPlanAppService;
    @Mock private TaskExecutor executor;

    private AiRecipeStreamController recipeStreamController;
    private AiMealPlanStreamController mealPlanStreamController;

    @BeforeEach
    void setup() {
        recipeStreamController = new AiRecipeStreamController(aiRecipeAppService, executor);
        mealPlanStreamController = new AiMealPlanStreamController(aiMealPlanAppService, executor);
    }

    @Nested
    @DisplayName("AiRecipeStreamController")
    class RecipeStreamTests {

        @Test
        @DisplayName("chatStream 返回非空 SseEmitter 并提交异步任务")
        void chatStream_returnsSseEmitterAndSubmitsTask() {
            // given
            var cmd = new AiRecipeParseChatCmd(null, "番茄炒蛋");

            // when
            SseEmitter result = recipeStreamController.chatStream(cmd);

            // then - 返回非空 SseEmitter
            assertThat(result).isNotNull();
            // then - executor 被调用提交异步任务
            verify(executor).execute(any(Runnable.class));
        }

        @Test
        @DisplayName("chatStream 支持带 sessionId 的续轮对话")
        void chatStream_withSessionId_returnsSseEmitter() {
            // given
            var cmd = new AiRecipeParseChatCmd("session-123", "加点辣椒");

            // when
            SseEmitter result = recipeStreamController.chatStream(cmd);

            // then
            assertThat(result).isNotNull();
            verify(executor).execute(any(Runnable.class));
        }
    }

    @Nested
    @DisplayName("AiMealPlanStreamController")
    class MealPlanStreamTests {

        @Test
        @DisplayName("generateStream 返回非空 SseEmitter 并提交异步任务")
        void generateStream_returnsSseEmitterAndSubmitsTask() {
            // given
            var cmd = new AiMealPlanGenerateCmd(1L, LocalDate.of(2026, 7, 14), null);

            // when
            SseEmitter result = mealPlanStreamController.generateStream(cmd);

            // then - 返回非空 SseEmitter
            assertThat(result).isNotNull();
            // then - executor 被调用提交异步任务
            verify(executor).execute(any(Runnable.class));
        }

        @Test
        @DisplayName("generateStream 支持带 userHint 的请求")
        void generateStream_withUserHint_returnsSseEmitter() {
            // given
            var cmd = new AiMealPlanGenerateCmd(1L, LocalDate.of(2026, 7, 14), "这周想吃清淡一些");

            // when
            SseEmitter result = mealPlanStreamController.generateStream(cmd);

            // then
            assertThat(result).isNotNull();
            verify(executor).execute(any(Runnable.class));
        }
    }
}
