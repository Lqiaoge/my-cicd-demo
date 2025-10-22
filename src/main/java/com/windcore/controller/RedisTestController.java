package com.windcore.controller;

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
public class RedisTestController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 测试Redis连接
     */
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
    @PostMapping("/set")
    public String setValue(@RequestParam String key, @RequestParam String value) {
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
    @GetMapping("/get")
    public String getValue(@RequestParam String key) {
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
    @DeleteMapping("/delete")
    public String deleteKey(@RequestParam String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            return deleted ? "删除成功" : "键不存在";
        } catch (Exception e) {
            return "删除失败: " + e.getMessage();
        }
    }
}