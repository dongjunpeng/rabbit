/**
 *
 */
package com.buterfleoge.rabbit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiezhenzong
 *
 */
public class RabbitWebContext {

    public static final String REQUEST_URI = "request_uri";
    public static final String REMOTE_IP = "remote_ip";
    public static final String REAL_IP = "real_ip";
    public static final String START_TIME = "start_time";

    private static final ThreadLocal<Map<String, Object>> context = new ThreadLocal<Map<String, Object>>();

    public static final void create() {
        context.set(new ConcurrentHashMap<String, Object>());
    }

    public static final void clear() {
        Map<String, Object> data = context.get();
        if (data != null) {
            data.clear();
        }
        context.remove();
    }

    public static final Object get(String key) {
        return context.get().get(key);
    }

    public static final void set(String key, Object value) {
        context.get().put(key, value);
    }

    public static final String getRequestURI() {
        return (String) get(REQUEST_URI);
    }

    public static final void setRequestURI(String uri) {
        set(REQUEST_URI, uri);
    }

    public static final String getRemoteIp() {
        return (String) get(REMOTE_IP);
    }

    public static final void setRemoteIp(String ip) {
        set(REMOTE_IP, ip);
    }

    public static final String getRealIp() {
        return (String) get(REAL_IP);
    }

    public static final void setRealIp(String ip) {
        set(REAL_IP, ip);
    }

    public static final long getStartTime() {
        return (long) get(START_TIME);
    }

    public static final void setStartTime(long startTime) {
        set(START_TIME, startTime);
    }

}
