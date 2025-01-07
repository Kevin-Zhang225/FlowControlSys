# User Flow Control System

## 项目简介
这是一个基于 Spring Boot 的限流器项目，用于限制用户对 API 的访问频率。

## 特性
- Version2 使用 Kafka Broker 和 Kafka Stream 来做实时计算 并将结果写回 Redis, 拦截器会将配置的 threshold 和 redis中的值进行比较 来决定是否方形。
- Version1 未使用 Kafka 相关的组件进行计算，通过 对Redis中的值 +1 然后更新回 Redis
- 可以灵活配置不同用户和不同 api group 的阈值
- 支持高并发请求
- 提供友好的超限提示

## 依赖
- Open JDK 22
- Spring Boot 2.7.5
- Redis (Run in local docker container)
- Kafka (Run in local docker container)
- Kafka Stream (Integrate with Springboot App)