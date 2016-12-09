package com.buterfleoge.rabbit.interceptor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StringUtils;

import com.buterfleoge.rabbit.WebConfig;
import com.buterfleoge.whale.Constants.CacheKey;
import com.buterfleoge.whale.Constants.DefaultValue;
import com.buterfleoge.whale.Utils;
import com.buterfleoge.whale.service.WeixinWebService;
import com.buterfleoge.whale.service.weixin.protocol.WxLoginScope;

/**
 *
 * @author xiezhenzong
 *
 */
public class WapInterceptor extends AuthInterceptor {

    @Autowired
    @Resource(name = "weixinCgibinService")
    private WeixinWebService weixinCgibinService;

    @Autowired
    @Resource(name = "cacheTemplate")
    private ValueOperations<String, Object> operations;

    @Value("${wx.login.base.callback}")
    private String baseCallback;

    @Override
    protected boolean shouldPreHandle(String path, HttpServletRequest request) {
        return !StringUtils.isEmpty(path) && isWeixinUserAgent(request) && !path.startsWith("/wx") && !path.contains("syserror")
                && !path.contains("notfound") && !path.contains("/notauth");
    }

    @Override
    protected String getAccesstokenKey(Long accountid) {
        return WebConfig.getWapAccessTokenKey(accountid);
    }

    @Override
    protected WeixinWebService getWeixinWebService() {
        return weixinCgibinService;
    }

    @Override
    protected boolean noAccountBasicInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String state = Utils.createNonceStr();
        String wxLoginUri = weixinCgibinService.getLoginUri(state, createCallback(request), WxLoginScope.SNSAPI_BASE);
        setState(state);
        response.sendRedirect(wxLoginUri);
        return false;
    }

    private String createCallback(HttpServletRequest request) throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder(baseCallback);
        builder.append("?origin=").append(request.getRequestURI());
        if (request.getQueryString() != null) {
            builder.append("?").append(request.getQueryString());
        }
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
