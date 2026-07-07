package io.yggdrasil.labs.mealmate.app.mealplan.parser;

/** AI 配餐结果解析异常。当 LLM 输出 JSON 无法反序列化时抛出。 */
public class AiMealPlanParseException extends RuntimeException {

    public AiMealPlanParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
