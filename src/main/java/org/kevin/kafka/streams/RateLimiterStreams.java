package org.kevin.kafka.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.kevin.kafka.ApiRequestProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Configuration
public class RateLimiterStreams {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public RateLimiterStreams(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @Bean
    public StreamsBuilder streamsBuilder() {
        return new StreamsBuilder();
    }

    @Bean
    public KStream<String, String> kStream(StreamsBuilder builder) {
        KStream<String, String> stream = builder.stream("api_requests", Consumed.with(Serdes.String(), Serdes.String()));

        stream.mapValues(value -> {
                    try {
                        return objectMapper.readValue(value, ApiRequestProducer.ApiRequestEvent.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter((key, event) -> event != null)
                .map((key, event) -> {
                    // 生成聚合 Key: userId:groupName:yyyyMMddHHmm
                    String time = Instant.ofEpochMilli(event.getTimestamp())
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
                    String aggKey = String.format("rate_limit:%s:%s:%s", event.getUserId(), event.getGroupName(), time);
                    return new KeyValue<>(aggKey, "1"); // Value用于计数
                })
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                .windowedBy(TimeWindows.of(Duration.ofMinutes(1)).advanceBy(Duration.ofMinutes(1)))
                .count(Materialized.as("api-counts"))
                .toStream()
                .foreach((windowedKey, count) -> {
                    // 将聚合结果写回 redis
                    String redisKey = windowedKey.key();
                    redisTemplate.opsForValue().set(redisKey, count.toString());
                    // 设置过期时间，确保键在1分钟后过期
                    redisTemplate.expire(redisKey, 1, TimeUnit.MINUTES);
                });

        return stream;
    }
}
