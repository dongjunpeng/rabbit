package com.buterfleoge.rabbit.interceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
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
public class LoginInterceptor implements HandlerInterceptor, Ordered {

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
        String path = request.getPathInfo();
        System.out.println(path);
        if (!shouldPreHandle(path)) {
            return true;
        }
        HttpSession session = request.getSession();
        AccountBasicInfo basicInfo = (AccountBasicInfo) session.getAttribute(SessionKey.ACCOUNT_BASIC_INFO);
        if (basicInfo != null) {
            Long accountid = basicInfo.getAccountInfo().getAccountid();
            WxAccessTokenResponse accessTokenResponse = (WxAccessTokenResponse) cacheTemplate.opsForValue()
                    .get(CacheKey.WX_ACCESS_TOKEN_PREFIX + DefaultValue.SEPARATOR + accountid);
            boolean isValid =
                    wxBiz.isAccessTokenValid(accessTokenResponse.getAccess_token(), accessTokenResponse.getOpenid());
            if (isValid) {
                WxAccessTokenResponse refreshToken = wxBiz.refreshToken(accessTokenResponse.getRefresh_token());
                cacheTemplate.opsForValue().set(CacheKey.WX_ACCESS_TOKEN_PREFIX + DefaultValue.SEPARATOR + accountid,
                        refreshToken); // 用refreshToken来更新accessToken
                return true;
            } else {
                response.sendRedirect("/wx/login");
                return false;
            }
        } else {
            Cookie[] cookies = request.getCookies();
            Long accountid = null;
            String token = null;
            if (ArrayUtils.isEmpty(cookies)) {
                response.sendRedirect("/wx/login");
                return false;
            }
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(CookieKey.ACCOUNTID)) {
                    accountid = Long.parseLong(cookie.getValue());
                } else if (cookie.getName().equals(CookieKey.TOKEN)) {
                    token = cookie.getValue();
                }
            }
            if (accountid == null || StringUtils.isEmpty(token)) {
                response.sendRedirect("/wx/login");
                return false;
            }
            String md5Token = Utils.stringMD5(accountid + DefaultValue.SEPARATOR + DefaultValue.TOKEN);
            if (!token.equals(md5Token)) {
                response.sendRedirect("/wx/login");
                return false;
            }
            AccountInfo info = accountInfoRepository.findByAccountid(accountid);
            AccountSetting setting = accountSettingRepository.findByAccountid(accountid);
            basicInfo = new AccountBasicInfo();
            basicInfo.setAccountInfo(info);
            basicInfo.setAccountSetting(setting);

            session.setAttribute(SessionKey.ACCOUNT_BASIC_INFO, basicInfo);
            return true;
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
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        if (path.startsWith("/order")) {
            return true;
        }
        if (path.startsWith("/account") && !path.equals("/account/basic")) {
            return true;
        }
        return false;
    }

}
