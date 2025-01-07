package org.kevin.interceptor;

import org.kevin.kafka.ApiRequestProducer;
import org.kevin.service.RateLimitService;
import org.kevin.util.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class RateLimiterInterceptor implements HandlerInterceptor {
    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private ApiRequestProducer apiRequestProducer;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = RequestUtil.getUserId(request);
        String apiPath = request.getRequestURI();

        boolean overLimit = rateLimitService.isOverLimit(userId, apiPath);
        if (overLimit) {
            // 超限，返回友好提示
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Please retry one minute later");
            return false; // 拦截请求
        }

        String groupName = rateLimitService.getGroupName(apiPath);
        long timestamp = System.currentTimeMillis();

        // 发送请求事件到 Kafka, 以供后续的实时计算
        apiRequestProducer.sendApiRequest(userId, groupName, apiPath, timestamp);

        return true; // 未超限，继续执行
    }
}
