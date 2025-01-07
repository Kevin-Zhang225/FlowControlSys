package org.kevin.service;

import org.kevin.config.LimitConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {
    private final RedisTemplate<String, String> redisTemplate;
    private final LimitConfigProperties limitConfigProperties;

    private final Map<String, String> apiToGroupMap = new HashMap<>();

    @Autowired
    public RateLimitService(RedisTemplate<String, String> redisTemplate, LimitConfigProperties limitConfigProperties) {
        this.redisTemplate = redisTemplate;
        this.limitConfigProperties = limitConfigProperties;
    }

    /**
     * Spring 初始化完成后，从 limitConfigProperties 中构建 "apiPath -> groupName"的映射
     */
    @PostConstruct
    public void initApiToGroupMap() {
        apiToGroupMap.clear();
        Map<String, LimitConfigProperties.GroupConfig> groups = limitConfigProperties.getGroups();
        // groups 中的key 就是groupName, value里面包含 apis
        for (Map.Entry<String, LimitConfigProperties.GroupConfig> entry : groups.entrySet()) {
            String groupName = entry.getKey();
            List<String> apis = entry.getValue().getApis();
            if (apis != null) {
                for (String api : apis) {
                    apiToGroupMap.put(api, groupName);
                }
            }
        }
    }

    /**
     * 检查是否查过限流
     * @param userId
     * @param apiPath
     * @return
     */
    public boolean isOverLimit(String userId, String apiPath) {
        // 根据apiPath 找到 group
        String groupName = apiToGroupMap.get(apiPath);
        // 生成分钟级key
        String minuteKey = generateMinuteKey(userId, groupName);
        Long currentCount = redisTemplate.opsForValue().increment(minuteKey, 1);
        if (currentCount == null) {
            return false;
        }
        if (currentCount == 1) {
            // 第一次设置过期时间为 60 秒
            redisTemplate.expire(minuteKey, 60, TimeUnit.SECONDS);
        }

        // 找到这个用户对应这个组的阈值
        int threshold = getThreshold(userId, groupName);

        // 判断是否超过了阈值
        return currentCount > threshold;
    }

    private int getThreshold(String userId, String groupName) {
        // thresholds: user -> (groupName -> threshold)
        Map<String, Map<String, Integer>> userThresholdMap = limitConfigProperties.getThresholds();
        Map<String, Integer> groupMap = userThresholdMap.get(userId);
        if (groupMap != null && groupMap.containsKey(groupName)) {
            return groupMap.get(groupName);
        }

        // 如果没有配置，使用一个默认值
        return 10000;
    }

    private String generateMinuteKey(String userId, String groupName) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        return String.format("rate_limit:%s:%s:%s", userId, groupName, time);
    }

    public String getGroupName(String apiPath) {
        return apiToGroupMap.get(apiPath);
    }
}
