# User Flow Control System

## 项目简介
这是一个基于 Spring Boot 的限流器项目，用于限制用户对 API 的访问频率。

## 特性
- 基于 Redis 的限流实现
- 可以灵活配置不同用户和不同 api group 的阈值
- 支持高并发请求
- 提供友好的超限提示

## 依赖
- Open JDK 22
- Spring Boot 2.7.5
- Redis