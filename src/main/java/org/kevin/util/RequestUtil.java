package org.kevin.util;

import javax.servlet.http.HttpServletRequest;

public class RequestUtil {
    private static final String USER_HEADER = "X-UserId";

    public static String getUserId(HttpServletRequest request) {
        String userId = request.getHeader(USER_HEADER);
        return (userId == null || userId.isEmpty()) ? "anonymous" : userId;
    }
}
