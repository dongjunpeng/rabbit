package com.buterfleoge.rabbit.interceptor;

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
    protected boolean shouldPreHandle(String path) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        return (path.startsWith(ACCOUNT_HOME_URL_PREFIX) && !path.equals("/account/basicinfo"))
                || path.startsWith(ORDER_URL_PREFIX);
    }

    @Override
    protected boolean noAccountBasicInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.sendRedirect(WebConfig.LOGIN_URL + "?redirect=" + request.getRequestURI());
        return false;
    }

    @Override
    protected boolean hasAccountBasicInfo(HttpServletRequest request, HttpServletResponse response, Long accountid)
            throws Exception {
        return true;
    }

}
