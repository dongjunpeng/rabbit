package com.buterfleoge.rabbit.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.buterfleoge.rabbit.WebConfig;

/**
 *
 * @author xiezhenzong
 *
 */
public class WxLoginInterceptor extends AuthInterceptor {

    @Override
    protected boolean shouldPreHandle(String path, HttpServletRequest request) {
        return WebConfig.WX_LOGIN_URL.equals(path);
    }

    @Override
    protected boolean hasAccountBasicInfo(HttpServletRequest request, HttpServletResponse response, Long accountid)
            throws Exception {
        response.sendRedirect(WebConfig.getAccountHomePage(accountid));
        return false;
    }

}
