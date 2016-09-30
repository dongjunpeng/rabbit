package com.buterfleoge.rabbit.interceptor;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;

import com.buterfleoge.rabbit.WebConfig;

/**
 *
 * @author xiezhenzong
 *
 */
public class LoginStateInterceptor extends AuthInterceptor {

    private static final String ACCOUNT_HOME_URL_PREFIX = WebConfig.ACCOUNT_HOME_URL_PREFIX;
    private static final String ORDER_URL_PREFIX = WebConfig.ORDER_URL_PREFIX;

    @Override
    protected boolean shouldPreHandle(String path, HttpServletRequest request) {
        if (StringUtils.isEmpty(path) && isWeixinUserAgent(request)) {
            return false;
        }
        return (path.startsWith(ACCOUNT_HOME_URL_PREFIX) && !path.equals("/account/basicinfo"))
                || path.startsWith(ORDER_URL_PREFIX);
    }

    @Override
    protected boolean noAccountBasicInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if ("GET".equals(request.getMethod())) {
            StringBuffer buffer = request.getRequestURL().append("?").append(request.getQueryString());
            response.sendRedirect(WebConfig.LOGIN_URL + "?redirect=" + URLEncoder.encode(buffer.toString(), "UTF-8"));
        } else {
            response.sendRedirect(WebConfig.LOGIN_URL);
        }
        return false;
    }

}
