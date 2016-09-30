package com.buterfleoge.rabbit.interceptor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StringUtils;

import com.buterfleoge.rabbit.WebConfig;
import com.buterfleoge.rabbit.controller.WxController;
import com.buterfleoge.whale.Constants.CacheKey;
import com.buterfleoge.whale.Constants.DefaultValue;
import com.buterfleoge.whale.service.WeixinWebService;
import com.buterfleoge.whale.service.weixin.protocol.WxLoginScope;

/**
 *
 * @author xiezhenzong
 *
 */
public class WapInterceptor extends AuthInterceptor {

    private static final String ACCOUNT_HOME_URL_PREFIX = WebConfig.ACCOUNT_HOME_URL_PREFIX;
    private static final String ORDER_URL_PREFIX = WebConfig.ORDER_URL_PREFIX;

    @Autowired
    @Resource(name = "weixinCgibinService")
    private WeixinWebService weixinCgibinService;

    @Autowired
    @Resource(name = "cacheTemplate")
    private ValueOperations<String, Object> operations;

    @Override
    protected boolean shouldPreHandle(String path, HttpServletRequest request) {
        if (StringUtils.isEmpty(path) && !isWeixinUserAgent(request)) {
            return false;
        }
        return (path.startsWith(ACCOUNT_HOME_URL_PREFIX) && !path.equals("/account/basicinfo"))
                || path.startsWith(ORDER_URL_PREFIX);
    }

    @Override
    protected String getAccesstokenKey(Long accountid) {
        return "m" + DefaultValue.SEPARATOR + super.getAccesstokenKey(accountid);
    }

    @Override
    protected WeixinWebService getWeixinWebService() {
        return weixinCgibinService;
    }

    @Override
    protected boolean noAccountBasicInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String state = WxController.createState();
        String wxLoginUri = weixinCgibinService.getLoginUri(state, createCallback(request), WxLoginScope.SNSAPI_BASE);
        setState(state);
        response.sendRedirect(wxLoginUri);
        return false;
    }

    private String createCallback(HttpServletRequest request) throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder("/wx/callback/base");
        builder.append("?redirect=").append(request.getRequestURL()).append("?").append(request.getQueryString());
        System.out.println("WapInteceptor createCallback: " + builder.toString());
        System.out.println("WapInteceptor createCallback: " + URLEncoder.encode(builder.toString(), "UTF-8"));
        return URLEncoder.encode(builder.toString(), "UTF-8");
    }

    private void setState(String state) {
        try {
            operations.set(getWxLoginStateKey(state), state);
        } catch (Exception e) {
            LOG.error("set state to cache failed, state: " + state, e);
        }
    }

    private String getWxLoginStateKey(String state) {
        return CacheKey.WX_LOGIN_STATE_PREFIX + DefaultValue.SEPARATOR + state;
    }
}
