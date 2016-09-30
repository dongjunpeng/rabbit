package com.buterfleoge.rabbit.interceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ValueOperations;

import com.buterfleoge.rabbit.WebConfig;
import com.buterfleoge.whale.Constants.SessionKey;
import com.buterfleoge.whale.service.WeixinWebService;
import com.buterfleoge.whale.service.weixin.protocol.WxAccessTokenResponse;
import com.buterfleoge.whale.type.protocol.account.object.AccountBasicInfo;

/**
 *
 * @author xiezhenzong
 *
 */
public abstract class AuthInterceptor extends RabbitInterceptor {

    @Autowired
    @Resource(name = "weixinWebService")
    private WeixinWebService weixinWebService;

    @Autowired
    @Resource(name = "cacheTemplate")
    private ValueOperations<String, Object> operations;

    @Override
    public boolean preHandle(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
        AccountBasicInfo basicInfo = (AccountBasicInfo) request.getSession().getAttribute(SessionKey.ACCOUNT_BASIC_INFO);
        if (basicInfo != null) {
            Long accountid = basicInfo.getAccountInfo().getAccountid();
            String accountTokenCacheKey = getAccesstokenKey(accountid);
            WxAccessTokenResponse accessToken = getAccessTokenFromCache(accountTokenCacheKey);
            if (accessToken != null
                    && getWeixinWebService().isAccessTokenValid(accessToken.getAccess_token(), accessToken.getOpenid())) {
                if (refreshToken(accessToken.getRefresh_token(), accountTokenCacheKey)) {
                    return hasAccountBasicInfo(request, response, accountid);
                }
            }
        }
        return noAccountBasicInfo(request, response);
    }

    protected String getAccesstokenKey(Long accountid) {
        return WebConfig.getAccessTokenKey(accountid);
    }

    protected WeixinWebService getWeixinWebService() {
        return weixinWebService;
    }

    protected boolean noAccountBasicInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return true;
    }

    protected boolean hasAccountBasicInfo(HttpServletRequest request, HttpServletResponse response, Long accountid) throws Exception {
        return true;
    }

    protected final boolean isWeixinUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        System.out.println("user agent: " + userAgent);
        return userAgent != null && userAgent.contains("weixin");
    }

    private WxAccessTokenResponse getAccessTokenFromCache(String accountTokenCacheKey) {
        try {
            return (WxAccessTokenResponse) operations.get(accountTokenCacheKey);
        } catch (Exception e) {
            LOG.error("get accessToken from cache failed, accountTokenCacheKey: " + accountTokenCacheKey, e);
            return null;
        }
    }

    private boolean refreshToken(String refreshToken, String accountTokenCacheKey) {
        try {
            WxAccessTokenResponse accessToken = getWeixinWebService().refreshToken(refreshToken);
            if (accessToken != null && accessToken.getErrcode() == null) {
                operations.set(accountTokenCacheKey, accessToken); // 用refreshToken来更新accessToken
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            LOG.error("refresh token failed, refreshToken: " + refreshToken + ", accountTokenKey: " + accountTokenCacheKey, e);
            return false;
        }
    }

}
