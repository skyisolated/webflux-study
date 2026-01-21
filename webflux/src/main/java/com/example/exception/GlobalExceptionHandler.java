package com.example.exception;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;

/**
 * 全局异常处理器，和以前一样的用法
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NullPointerException.class)
    public String handleException(Exception e) {
        JSONObject info = new JSONObject();
        info.put("code", 500);
        info.put("message", e.getMessage());
        info.put("data", Collections.emptyList());
        return info.toJSONString();
    }

    /**
     * springboot3新引入的一种错误返回类型
     * @param e
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleRuntimeException(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
}
