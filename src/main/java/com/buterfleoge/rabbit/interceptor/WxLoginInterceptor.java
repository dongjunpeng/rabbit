package com.buterfleoge.rabbit.interceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.buterfleoge.whale.Constants.CacheKey;
import com.buterfleoge.whale.Constants.CookieKey;
import com.buterfleoge.whale.Constants.DefaultValue;
import com.buterfleoge.whale.Constants.SessionKey;
import com.buterfleoge.whale.Utils;
import com.buterfleoge.whale.biz.account.WxBiz;
import com.buterfleoge.whale.dao.AccountInfoRepository;
import com.buterfleoge.whale.dao.AccountSettingRepository;
import com.buterfleoge.whale.type.AccountBasicInfo;
import com.buterfleoge.whale.type.entity.AccountInfo;
import com.buterfleoge.whale.type.entity.AccountSetting;
import com.buterfleoge.whale.type.protocol.wx.WxAccessTokenResponse;

/**
 *
 * @author xiezhenzong
 *
 */
public class WxLoginInterceptor implements HandlerInterceptor, Ordered {

    private static final Logger LOG = LoggerFactory.getLogger(WxLoginInterceptor.class);

    @Autowired
    private WxBiz wxBiz;

    @Autowired
    private RedisTemplate<String, Object> cacheTemplate;

    @Autowired
    private AccountInfoRepository accountInfoRepository;

    @Autowired
    private AccountSettingRepository accountSettingRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = request.getRequestURI();
        System.out.println(path);
        if (!shouldPreHandle(path)) {
            return true;
        }
        HttpSession session = request.getSession();
        AccountBasicInfo basicInfo = (AccountBasicInfo) session.getAttribute(SessionKey.ACCOUNT_BASIC_INFO);
        if (basicInfo != null) {
            try {
                ValueOperations<String, Object> operations = cacheTemplate.opsForValue();
                Long accountid = basicInfo.getAccountInfo().getAccountid();
                WxAccessTokenResponse accessTokenResponse =
                        (WxAccessTokenResponse) operations.get(getAccessTokenKey(accountid));
                if (accessTokenResponse == null) {
                    return true;
                }
                boolean isValid = wxBiz.isAccessTokenValid(accessTokenResponse.getAccess_token(),
                        accessTokenResponse.getOpenid());
                if (isValid) {
                    WxAccessTokenResponse refreshToken = wxBiz.refreshToken(accessTokenResponse.getRefresh_token());
                    operations.set(getAccessTokenKey(accountid), refreshToken); // 用refreshToken来更新accessToken
                    response.sendRedirect("/account/" + accountid);
                    return false;
                } else {
                    return true;
                }
            } catch (Exception e) {
                LOG.error("validate access token failed", e);
                response.sendRedirect("/wx/failed");
                return false;
            }
        } else {
            Cookie[] cookies = request.getCookies();
            if (ArrayUtils.isEmpty(cookies)) {
                return true;
            }
            Long accountid = null;
            String token = null;
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(CookieKey.ACCOUNTID)) {
                    accountid = Long.parseLong(cookie.getValue());
                } else if (cookie.getName().equals(CookieKey.TOKEN)) {
                    token = cookie.getValue();
                }
            }
            if (accountid == null || StringUtils.isEmpty(token)) {
                return true;
            }
            String md5Token = createToken(accountid);
            if (!token.equals(md5Token)) {
                return true;
            }
            try {
                AccountInfo info = accountInfoRepository.findByAccountid(accountid);
                if (info == null) {
                    LOG.error("accountid in cookie is invalid, accountid: " + accountid + ", token: " + token);
                    return true;
                }
                AccountSetting setting = accountSettingRepository.findByAccountid(accountid);
                addBasicInfoToSession(info, setting, session);
                response.sendRedirect("/account/" + accountid);
                return false;
            } catch (Exception e) {
                LOG.error("query account in db failed", e);
                response.sendRedirect("/wx/failed");
                return false;
            }
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

    private boolean shouldPreHandle(String path) {
        return StringUtils.hasText(path) && path.equals("/wx/login");
    }

    private String getAccessTokenKey(Long accountid) {
        return CacheKey.WX_ACCESS_TOKEN_PREFIX + DefaultValue.SEPARATOR + accountid;
    }

    private String createToken(Long accountid) {
        return Utils.stringMD5(accountid + DefaultValue.SEPARATOR + DefaultValue.TOKEN);
    }

    private void addBasicInfoToSession(AccountInfo info, AccountSetting setting, HttpSession session) {
        AccountBasicInfo basicInfo = new AccountBasicInfo();
        try {
            basicInfo.setAccountInfo(info);
            basicInfo.setAccountSetting(setting);
        } catch (Exception e) {

        }
        session.setAttribute(SessionKey.ACCOUNT_BASIC_INFO, basicInfo);
    }
}
