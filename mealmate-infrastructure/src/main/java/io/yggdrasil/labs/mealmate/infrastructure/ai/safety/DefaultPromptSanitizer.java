package io.yggdrasil.labs.mealmate.infrastructure.ai.safety;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import io.yggdrasil.labs.mealmate.domain.common.ai.PromptSanitizer;

/**
 * Prompt 输入清洗默认实现。
 *
 * <p>三层防御：截断超长输入 → 移除 markdown 代码块 → 过滤注入模式。 黑名单作为第一层防御，核心安全保障来自结构化隔离（system prompt 与 user 输入分离）。
 */
@Component
public class DefaultPromptSanitizer implements PromptSanitizer {

    private static final int MAX_LENGTH = 2000;

    /** markdown 代码块正则（匹配 ```...``` 包裹的内容） */
    private static final Pattern CODE_BLOCK_PATTERN =
            Pattern.compile("```[\\s\\S]*?```", Pattern.MULTILINE);

    /** 已知注入模式关键词（小写匹配） */
    private static final List<String> INJECTION_PATTERNS =
            List.of(
                    "ignore previous",
                    "ignore all previous",
                    "system:",
                    "你现在是",
                    "forget all",
                    "new instructions",
                    "扮演",
                    "disregard");

    @Override
    public String sanitize(String userInput) {
        if (userInput == null || userInput.isEmpty()) {
            return "";
        }

        String result = userInput;

        // 1. 截断超长输入
        if (result.length() > MAX_LENGTH) {
            result = result.substring(0, MAX_LENGTH);
        }

        // 2. 移除 markdown 代码块
        result = CODE_BLOCK_PATTERN.matcher(result).replaceAll("");
        // 移除残留的未闭合 ``` 标记
        result = result.replace("```", "");

        // 3. 过滤注入模式
        String lowerResult = result.toLowerCase();
        for (String pattern : INJECTION_PATTERNS) {
            if (lowerResult.contains(pattern)) {
                result = result.replaceAll("(?i)" + Pattern.quote(pattern), "[filtered]");
                lowerResult = result.toLowerCase();
            }
        }

        return result.trim();
    }
}
