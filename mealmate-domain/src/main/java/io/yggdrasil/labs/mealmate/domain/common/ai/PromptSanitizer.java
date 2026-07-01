package io.yggdrasil.labs.mealmate.domain.common.ai;

/**
 * Prompt 输入清洗接口。
 *
 * <p>domain 层定义，infrastructure 层提供默认实现。 负责截断超长输入、移除危险内容和过滤注入模式。
 */
public interface PromptSanitizer {

    /**
     * 清洗用户输入。
     *
     * @param userInput 用户原始输入
     * @return 清洗后的安全输入
     */
    String sanitize(String userInput);
}
