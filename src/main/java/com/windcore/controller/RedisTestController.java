package com.windcore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * Redis测试控制器
 * 
 * @author windcore
 */
@RestController
@RequestMapping("/api/redis")
@Tag(name = "Redis服务", description = "提供Redis缓存操作功能，包括连接测试、键值操作等")
public class RedisTestController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 测试Redis连接
     */
    @Operation(
        summary = "测试Redis连接",
        description = "测试Redis服务器连接是否正常，并进行基本的读写操作",
        tags = {"连接测试"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Redis连接正常"),
        @ApiResponse(responseCode = "500", description = "Redis连接失败")
    })
    @GetMapping("/test")
    public String testRedis() {
        try {
            // 测试设置值
            redisTemplate.opsForValue().set("test:key", "Hello Redis!", 60, TimeUnit.SECONDS);
            
            // 测试获取值
            String value = (String) redisTemplate.opsForValue().get("test:key");
            
            return "Redis连接正常！存储的值: " + value;
        } catch (Exception e) {
            return "Redis连接失败: " + e.getMessage();
        }
    }

    /**
     * 设置键值对
     */
    @Operation(
        summary = "设置Redis键值对",
        description = "在Redis中设置键值对，默认过期时间为300秒",
        tags = {"键值操作"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "设置成功"),
        @ApiResponse(responseCode = "500", description = "设置失败")
    })
    @PostMapping("/set")
    public String setValue(
        @Parameter(description = "Redis键名", required = true, example = "user:123")
        @RequestParam String key, 
        @Parameter(description = "Redis键值", required = true, example = "张三")
        @RequestParam String value) {
        try {
            redisTemplate.opsForValue().set(key, value, 300, TimeUnit.SECONDS);
            return "设置成功: " + key + " = " + value;
        } catch (Exception e) {
            return "设置失败: " + e.getMessage();
        }
    }

    /**
     * 获取值
     */
    @Operation(
        summary = "获取Redis键值",
        description = "根据键名获取Redis中存储的值",
        tags = {"键值操作"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "获取失败")
    })
    @GetMapping("/get")
    public String getValue(
        @Parameter(description = "Redis键名", required = true, example = "user:123")
        @RequestParam String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            return value != null ? "值: " + value.toString() : "键不存在或已过期";
        } catch (Exception e) {
            return "获取失败: " + e.getMessage();
        }
    }

    /**
     * 删除键
     */
    @Operation(
        summary = "删除Redis键",
        description = "根据键名删除Redis中的键值对",
        tags = {"键值操作"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "500", description = "删除失败")
    })
    @DeleteMapping("/delete")
    public String deleteKey(
        @Parameter(description = "Redis键名", required = true, example = "user:123")
        @RequestParam String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            return deleted ? "删除成功" : "键不存在";
        } catch (Exception e) {
            return "删除失败: " + e.getMessage();
        }
    }
}