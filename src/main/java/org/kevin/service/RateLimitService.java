package org.kevin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {
    private final RedisTemplate<String, String> redisTemplate;

    // Set default threshold
    @Value("${ratelimit.defaultThreshold:10000}")
    private int defaultThreshold;

    public RateLimitService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 检查是否查过限流
     * @param userId
     * @param apiPath
     * @return
     */
    public boolean isOverLimit(String userId, String apiPath) {
        // 生成分钟级key
        String minuteKey = generateMinuteKey(userId, apiPath);
        Long currentCount = redisTemplate.opsForValue().increment(minuteKey, 1);
        if (currentCount == 1) {
            // 第一次设置过期时间为 60 秒
            redisTemplate.expire(minuteKey, 60, TimeUnit.SECONDS);
        }

        // 判断是否超过了阈值
        return currentCount > defaultThreshold;
    }

    private String generateMinuteKey(String userId, String apiPath) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        return String.format("rate_limit:%s:%s:%s", userId, apiPath, time);
    }
}
