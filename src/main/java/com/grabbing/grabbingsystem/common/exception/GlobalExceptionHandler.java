package com.grabbing.grabbingsystem.common.exception;

import com.grabbing.grabbingsystem.common.api.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * GlobalExceptionHandler：全局异常处理器
 *
 * 它的作用：
 * 1) 不管哪里出错，都不会返回“白页/默认错误页”
 * 2) 统一返回 ApiResponse.fail(...) 的 JSON
 *
 * @RestControllerAdvice 相当于：
 * - @ControllerAdvice（全局拦截异常）
 * - + @ResponseBody（返回 JSON）
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 1）处理 @Valid 校验失败（通常是请求体 JSON 校验失败）
     *
     * 例如：你在 DTO 上写了 @NotBlank，前端传空字符串就会触发
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {

        // 取第一个字段错误，给新手更直观（你后面也可以拼接全部错误）
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .orElse("参数校验失败");

        // 40001：你自定义的参数错误码（随你改）
        return ApiResponse.fail(40001, msg);
    }

    /**
     * 2）处理单个参数校验失败（比如 @RequestParam 上的 @Min / @NotBlank 等）
     *
     * 例如：/ping/validate?count=0 触发 @Min(1)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraintViolation(ConstraintViolationException e) {
        return ApiResponse.fail(40001, e.getMessage());
    }

    /**
     * 3）处理业务异常（你自己主动抛的 BizException）
     */
    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBizException(BizException e) {
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<?> handleBusinessException(BusinessException e) {
        return ApiResponse.fail(
                e.getErrorCode().getCode(),
                e.getErrorCode().getMsg()
        );
    }
    /**
     * 4）兜底：任何没想到的异常都在这里接住
     *
     * 这样不会把堆栈信息暴露给前端（安全 + 体验更好）
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleUnknown(Exception e) {
        e.printStackTrace();
        return ApiResponse.fail(50000, "系统异常");
    }
}