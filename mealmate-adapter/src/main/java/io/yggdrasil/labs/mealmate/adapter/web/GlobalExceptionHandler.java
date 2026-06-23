package io.yggdrasil.labs.mealmate.adapter.web;

import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.alibaba.cola.dto.Response;

import io.yggdrasil.labs.mealmate.domain.common.exception.BizException;

/** 全局异常处理器。将业务异常转换为 COLA Response 格式，保证前端始终收到结构化错误信息。 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务异常（BizException），携带结构化错误码。 */
    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Response handleBizException(BizException e) {
        log.warn("业务异常: [{}] {}", e.getErrCode(), e.getMessage());
        return Response.buildFailure(e.getErrCode(), e.getMessage());
    }

    /** 业务参数异常（如 PLAN_NOT_FOUND、ITEM_NOT_FOUND）。 */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response handleIllegalArgument(IllegalArgumentException e) {
        log.warn("业务参数异常: {}", e.getMessage());
        return Response.buildFailure(e.getMessage(), e.getMessage());
    }

    /** 业务状态异常（如 MEAL_PLAN_ALREADY_CONFIRMED）。 */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Response handleIllegalState(IllegalStateException e) {
        log.warn("业务状态异常: {}", e.getMessage());
        return Response.buildFailure(e.getMessage(), e.getMessage());
    }

    /** Bean Validation 校验失败（@Valid 触发）。 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response handleMethodArgNotValid(MethodArgumentNotValidException e) {
        String msg =
                e.getBindingResult().getFieldErrors().stream()
                        .map(f -> f.getField() + ": " + f.getDefaultMessage())
                        .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", msg);
        return Response.buildFailure("VALIDATION_ERROR", msg);
    }

    /** Constraint Violation（@Validated 触发）。 */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response handleConstraintViolation(ConstraintViolationException e) {
        String msg =
                e.getConstraintViolations().stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .collect(Collectors.joining("; "));
        log.warn("约束校验失败: {}", msg);
        return Response.buildFailure("VALIDATION_ERROR", msg);
    }

    /** 未预期异常兜底。 */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response handleUnknown(Exception e) {
        log.error("未预期异常", e);
        return Response.buildFailure("INTERNAL_ERROR", "服务器内部错误");
    }
}
