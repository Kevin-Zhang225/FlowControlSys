package org.kevin.interceptor;

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

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = RequestUtil.getUserId(request);
        String apiPath = request.getRequestURI();

        boolean overLimit = rateLimitService.isOverLimit(userId, apiPath);
        if (overLimit) {
            // 超限，返回友好提示
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("访问频率过高，请稍后再试");
            System.out.println("访问频率过高，请稍后再试");
            return false; // 拦截请求
        }

        return true; // 未超限，继续执行
    }
}
