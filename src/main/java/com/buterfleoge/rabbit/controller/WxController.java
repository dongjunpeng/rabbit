package com.buterfleoge.rabbit.controller;

import java.net.URLEncoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.buterfleoge.whale.Constants.CacheKey;
import com.buterfleoge.whale.Constants.CookieKey;
import com.buterfleoge.whale.Constants.DefaultValue;
import com.buterfleoge.whale.Constants.SessionKey;
import com.buterfleoge.whale.Utils;
import com.buterfleoge.whale.biz.account.WxBiz;
import com.buterfleoge.whale.dao.AccountContactsRepository;
import com.buterfleoge.whale.dao.AccountInfoRepository;
import com.buterfleoge.whale.dao.AccountSettingRepository;
import com.buterfleoge.whale.type.entity.AccountInfo;
import com.buterfleoge.whale.type.entity.AccountSetting;
import com.buterfleoge.whale.type.enums.Gender;
import com.buterfleoge.whale.type.protocol.account.object.AccountBasicInfo;
import com.buterfleoge.whale.type.protocol.wx.WxAccessTokenResponse;
import com.buterfleoge.whale.type.protocol.wx.WxUserinfoResponse;

/**
 *
 * @author xiezhenzong
 *
 */
// @Controller
@RequestMapping("/wx")
public class WxController implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(WxController.class);

    @Value("${wx.login.callback}")
    private String wxLoginCallback;

    @Autowired
    private WxBiz wxBiz;

    @Autowired
    private AccountSettingRepository accountSettingRepository;

    @Autowired
    private AccountInfoRepository accountInfoRepository;

    @Autowired
    private AccountContactsRepository accountContactsRepository;

    @Autowired
    private RedisTemplate<String, Object> cacheTemplate;

    @RequestMapping(value = "/login")
    public void wxLogin(HttpServletRequest request, HttpServletResponse httpResponse) throws Exception {
        String state = createState();
        String redirectUri = createRedirectUri();
        String wxLoginUri = wxBiz.getLoginUri(state, redirectUri);
        request.getSession().setAttribute(SessionKey.WX_LOGIN_STATE, state);
        httpResponse.sendRedirect(wxLoginUri);
    }

    @RequestMapping(value = "/callback")
    public void wxCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("/wx/failed");
            return;
        }
        String sessionState = getState(session);
        String code = request.getParameter("code");
        String wxState = request.getParameter("state");
        if (StringUtils.isEmpty(code) || sessionState == null || !sessionState.equals(wxState)) {
            response.sendRedirect("/wx/failed");
            return;
        }
        WxAccessTokenResponse accessToken = null;
        WxUserinfoResponse userinfoResponse = null;
        try {
            accessToken = wxBiz.getAccessToken(code);
            userinfoResponse = wxBiz.getUserinfo(accessToken.getAccess_token(), accessToken.getOpenid());
        } catch (Exception e) {
            LOG.error("call wx failed", e);
            response.sendRedirect("/wx/failed");
            return;
        }
        AccountInfo info = null;
        AccountSetting setting = null;
        try {
            setting = accountSettingRepository.findByWxid(userinfoResponse.getUnionid());
            if (setting != null) {
                info = accountInfoRepository.findByAccountid(setting.getAccountid());
                addBasicInfoToSession(info, setting, session);
                addCookie(String.valueOf(info.getAccountid()), response);
                addAccessTokenToCache(info.getAccountid(), accessToken);
                response.sendRedirect("/account/" + info.getAccountid());
                return;
            }
        } catch (Exception e) {
            LOG.error("find account in db failed", e);
            response.sendRedirect("/wx/failed");
            return;
        }

        info = createAccountInfo();
        try {
            info = accountInfoRepository.save(info);
            setting = createAccountSetting(userinfoResponse, info);
            setting = accountSettingRepository.save(setting);
        } catch (Exception e) {
            LOG.error("create account failed", e);
            response.sendRedirect("/wx/failed");
            return;
        }
        addBasicInfoToSession(info, setting, session);
        addCookie(String.valueOf(info.getAccountid()), response);
        addAccessTokenToCache(info.getAccountid(), accessToken);
        response.sendRedirect("/account/" + info.getAccountid());
    }

    private static String createState() {
        StringBuilder sb = new StringBuilder(DefaultValue.TOKEN) //
                .append(DefaultValue.SEPARATOR).append(System.currentTimeMillis()) //
                .append(DefaultValue.SEPARATOR).append(Math.random());
        String stringMd5 = Utils.stringMD5(sb.toString());
        return stringMd5;
    }

    private String createRedirectUri() throws Exception {
        StringBuilder sb = new StringBuilder(wxLoginCallback).append("/wx/callback");
        return URLEncoder.encode(sb.toString(), "UTF-8");
    }

    private String getState(HttpSession session) {
        String state = (String) session.getAttribute(SessionKey.WX_LOGIN_STATE);
        return StringUtils.hasText(state) ? state : null;
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

    private void addCookie(String accountid, HttpServletResponse response) {
        response.addCookie(createCookie(CookieKey.ACCOUNTID, accountid));
        response.addCookie(createCookie(CookieKey.TOKEN, createToken(accountid)));
    }

    private Cookie createCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(DefaultValue.COOKIE_EXPIRY);
        return cookie;
    }

    private String createToken(String accountid) {
        return Utils.stringMD5(accountid + DefaultValue.SEPARATOR + DefaultValue.TOKEN);
    }

    private void addAccessTokenToCache(Long accountid, WxAccessTokenResponse accessToken) {
        String cacheKey = CacheKey.WX_ACCESS_TOKEN_PREFIX + DefaultValue.SEPARATOR + accountid;
        cacheTemplate.opsForValue().set(cacheKey, accessToken);
    }

    protected AccountInfo createAccountInfo() {
        AccountInfo info = new AccountInfo();
        info.setAddTime(System.currentTimeMillis());
        info.setModTime(info.getAddTime());
        return info;
    }

    protected AccountSetting createAccountSetting(WxUserinfoResponse userinfoResponse, AccountInfo info) {
        AccountSetting setting = new AccountSetting();
        setting.setAccountid(info.getAccountid());
        setting.setNickname(userinfoResponse.getNickname());
        setting.setWxname(userinfoResponse.getNickname());
        setting.setWxid(userinfoResponse.getUnionid());
        setting.setAvatarUrl(userinfoResponse.getHeadimgurl());
        setting.setGender(getGender(userinfoResponse.getSex()));
        setting.setModTime(info.getAddTime());
        return setting;
    }

    private Gender getGender(int sex) {
        if (sex == WxUserinfoResponse.SEX_MALE) {
            return Gender.MALE;
        } else if (sex == WxUserinfoResponse.SEX_FEMALE) {
            return Gender.FEMALE;
        } else {
            return Gender.UNKNOW;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        cacheTemplate.opsForValue().set("dkdkdk", new WxAccessTokenResponse());

    }
}
