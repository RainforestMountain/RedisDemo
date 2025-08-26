package com.example.redisdemo_1.lua;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class RedisService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;


    // ========================== 基础命令 ==========================

    /**
     * 设置键值对
     */
    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置键值对并指定过期时间
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取值
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    // ========================== 执行 Lua 脚本 ==========================

    /**
     * 执行 Lua 脚本（无返回值）
     *
     * @param script Lua 脚本内容
     * @param keys   键列表
     * @param args   参数列表
     */
    public void executeScript(String script, List<String> keys, Object... args) {
        DefaultRedisScript<Void> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(Void.class);
        stringRedisTemplate.execute(redisScript, keys, args);
    }

    /**
     * 执行 Lua 脚本（有返回值）
     *
     * @param script     Lua 脚本内容
     * @param returnType 返回值类型
     * @param keys       键列表
     * @param args       参数列表
     */
    public <T> T executeScriptWithResult(String script, Class<T> returnType,
                                         List<String> keys, Object... args) {
        DefaultRedisScript<T> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(returnType);
        return stringRedisTemplate.execute(
                redisScript,
                keys,
                args
        );
    }

    // ========================== 分布式锁示例（基于 Lua 脚本） ==========================

    /**
     * 释放分布式锁（使用 Lua 脚本保证原子性）
     *
     * @param lockKey     锁的键名
     * @param uniqueValue 加锁时的唯一标识（如 UUID）
     * @return true 释放成功，false 释放失败
     */
    public Boolean releaseLock(String lockKey, String uniqueValue) {
        // Lua 脚本：检查锁是否属于当前线程，是则释放
        String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "    return redis.call('del', KEYS[1]) " +
                "else " +
                "    return 0 " +
                "end";
        // 执行脚本（1 个键，1 个参数）
        return executeScriptWithResult(
                luaScript,
                Boolean.class,
                Collections.singletonList(lockKey),  // KEYS = [lockKey]
                uniqueValue                          // ARGV = [uniqueValue]
        );
    }

}