/**
 *
 */
package com.buterfleoge.rabbit.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.buterfleoge.rabbit.RabbitWebContext;

/**
 * @author xiezhenzong
 *
 */
public class RabbitWebContextInterceptor extends RabbitInterceptor {

    @Override
    protected boolean preHandle(String path, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        RabbitWebContext.create();
        RabbitWebContext.setRequestURI(path);
        String remoteIp = getIpAddrByRequest(request);
        String realIp = getRealIp(remoteIp);
        RabbitWebContext.setRemoteIp(remoteIp);
        RabbitWebContext.setRealIp(realIp);
        RabbitWebContext.setStartTime(System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        RabbitWebContext.clear();
    }

    private static String getIpAddrByRequest(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private String getRealIp(String ip) {
        return ip == null ? null : ip.contains(",") ? ip.split(",")[0].trim() : ip;
    }

}
