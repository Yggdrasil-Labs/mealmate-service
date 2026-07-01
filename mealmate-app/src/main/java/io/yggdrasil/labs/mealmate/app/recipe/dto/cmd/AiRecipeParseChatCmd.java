package io.yggdrasil.labs.mealmate.app.recipe.dto.cmd;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** AI 菜品解析对话命令。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiRecipeParseChatCmd {

    /** 会话 ID，首轮为 null */
    private String sessionId;

    /** 用户输入消息 */
    @NotBlank private String message;
}
