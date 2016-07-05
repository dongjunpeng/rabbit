package com.buterfleoge.rabbit.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author xiezhenzong
 *
 */
public abstract class RabbitInterceptor implements HandlerInterceptor {

    protected static final Logger LOG = LoggerFactory.getLogger(RabbitInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!shouldPreHandle(request.getRequestURI())) {
            return true;
        }
        return preHandle(request.getRequestURI(), request, response);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
            throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
    }

    protected boolean shouldPreHandle(String path) {
        return true;
    }

    protected abstract boolean preHandle(String path, HttpServletRequest request, HttpServletResponse response) throws Exception;

}
