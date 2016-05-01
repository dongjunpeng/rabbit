package com.buterfleoge.rabbit.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.buterfleoge.whale.Constants.SessionKey;
import com.buterfleoge.whale.type.AccountBasicInfo;

/**
 *
 * @author xiezhenzong
 *
 */
public abstract class AuthInterceptor implements HandlerInterceptor, Ordered {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = request.getRequestURI();
        System.out.println(path);
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        if (!shouldPreHandle(path)) {
            return true;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new IllegalStateException("");
        }
        
        AccountBasicInfo basicInfo = (AccountBasicInfo) session.getAttribute(SessionKey.ACCOUNT_BASIC_INFO);
        if (basicInfo == null) {
            throw new IllegalStateException("");
        }

        if (hasAuth(basicInfo.getAccountInfo().getAccountid(), request, path)) {
            return true;
        } else {
            response.sendRedirect("/noauth");
            return false;
        }
    }

    protected abstract boolean shouldPreHandle(String path);

    protected abstract boolean hasAuth(Long accountid, HttpServletRequest request, String path);

    protected Long getAccountidFromReq(HttpServletRequest request) {
        String temp = request.getParameter("accountid");
        return StringUtils.isEmpty(temp) ? null : Long.parseLong(temp);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
    }

}
