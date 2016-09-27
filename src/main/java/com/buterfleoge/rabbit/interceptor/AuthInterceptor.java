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
    private WeixinWebService weixinWebService;

    @Resource(name = "cacheTemplate")
    private ValueOperations<String, Object> operations;

    @Override
    public boolean preHandle(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
        AccountBasicInfo basicInfo = (AccountBasicInfo) request.getSession().getAttribute(SessionKey.ACCOUNT_BASIC_INFO);
        if (basicInfo != null) {
            Long accountid = basicInfo.getAccountInfo().getAccountid();
            String accountTokenCacheKey = WebConfig.getAccessTokenKey(accountid);
            WxAccessTokenResponse accessToken = getAccessTokenFromCache(accountTokenCacheKey);
            if (accessToken != null
                    && weixinWebService.isAccessTokenValid(accessToken.getAccess_token(), accessToken.getOpenid())) {
                if (refreshToken(accessToken.getRefresh_token(), accountTokenCacheKey)) {
                    return hasAccountBasicInfo(request, response, accountid);
                }
            }
        }
        return noAccountBasicInfo(request, response);
    }

    protected abstract boolean noAccountBasicInfo(HttpServletRequest request, HttpServletResponse response) throws Exception;

    protected abstract boolean hasAccountBasicInfo(HttpServletRequest request, HttpServletResponse response, Long accountid)
            throws Exception;

    private WxAccessTokenResponse getAccessTokenFromCache(String accountTokenCacheKey) {
        try {
            return (WxAccessTokenResponse) operations.get(accountTokenCacheKey);
        } catch (Exception e) {
            LOG.error("get accessToken from cache failed, accountTokenCacheKey: " + accountTokenCacheKey, e);
            return null;
        }
    }

    private boolean refreshToken(String refreshToken, String accountTokenCacheKey) {
        // FIXME: 后台进程refresh token?
        try {
            WxAccessTokenResponse accessToken = weixinWebService.refreshToken(refreshToken);
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
