package io.yggdrasil.labs.mealmate.domain.common.ai;

import io.yggdrasil.labs.mealmate.domain.common.exception.BizException;

/** AI 能力相关错误码。 */
public enum AiErrorCode implements BizException.ErrorCode {
    AI_AUTH_FAILURE("AI_AUTH_FAILURE", "AI 服务认证失败"),
    AI_RATE_LIMITED("AI_RATE_LIMITED", "AI 服务请求频率超限"),
    AI_SERVICE_UNAVAILABLE("AI_SERVICE_UNAVAILABLE", "AI 服务暂不可用"),
    AI_RESPONSE_INVALID("AI_RESPONSE_INVALID", "AI 响应格式异常"),
    AI_SESSION_NOT_FOUND("AI_SESSION_NOT_FOUND", "会话不存在或已过期"),
    ;

    private final String code;
    private final String message;

    AiErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
