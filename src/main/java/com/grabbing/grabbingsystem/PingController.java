package com.grabbing.grabbingsystem;

import com.grabbing.grabbingsystem.common.api.ApiResponse;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * PingController：健康检查接口
 *
 * 真实项目里：
 * - /ping 用于快速判断服务是不是活着
 * - K8s / LB / 监控系统也会调用类似接口
 */
@Validated
// @Validated 的作用：开启“方法参数”的校验能力（比如 @RequestParam 上的 @Min）
@RestController
public class PingController {

    /**
     * 最基本的健康接口
     */
    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        return ApiResponse.ok("ok");
    }

    /**
     * 用来演示“参数校验异常”是否会被全局异常捕获
     *
     * 访问：/ping/validate?count=0
     * 因为 @Min(1)，0 不合法，就会抛 ConstraintViolationException
     * 然后被 GlobalExceptionHandler 统一返回 JSON
     */
    @GetMapping("/ping/validate")
    public ApiResponse<Integer> validate(@RequestParam @Min(1) Integer count) {
        return ApiResponse.ok(count);
    }
}