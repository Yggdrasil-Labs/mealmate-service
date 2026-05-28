package io.yggdrasil.labs.mealmate.adapter.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.alibaba.cola.dto.Response;

import lombok.extern.slf4j.Slf4j;

/** 全局异常处理，将业务异常映射为结构化响应，不泄露内部信息。 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Bad request: {}", e.getMessage());
        return buildFailResponse(e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Response handleIllegalState(IllegalStateException e) {
        log.warn("Conflict: {}", e.getMessage());
        return buildFailResponse(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response handleUnexpected(Exception e) {
        log.error("Unexpected error", e);
        return buildFailResponse("INTERNAL_ERROR");
    }

    private Response buildFailResponse(String errCode) {
        Response response = new Response();
        response.setSuccess(false);
        response.setErrCode(errCode);
        response.setErrMessage(errCode);
        return response;
    }
}
