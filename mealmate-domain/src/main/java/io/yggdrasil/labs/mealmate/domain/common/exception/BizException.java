package io.yggdrasil.labs.mealmate.domain.common.exception;

/**
 * 业务异常基类。
 *
 * <p>携带结构化错误码和用户可读消息，由 GlobalExceptionHandler 统一转换为 COLA Response。 领域层和应用层均可抛出，替代裸
 * IllegalArgumentException/IllegalStateException。
 */
public class BizException extends RuntimeException {

    private final String errCode;

    public BizException(String errCode, String message) {
        super(message);
        this.errCode = errCode;
    }

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errCode = errorCode.getCode();
    }

    public String getErrCode() {
        return errCode;
    }

    /** 错误码契约接口，由各领域枚举实现。 */
    public interface ErrorCode {
        String getCode();

        String getMessage();
    }
}
