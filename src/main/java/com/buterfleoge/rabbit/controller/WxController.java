package com.buterfleoge.rabbit.controller;

import java.net.URLEncoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
import com.buterfleoge.whale.dao.AccountInfoRepository;
import com.buterfleoge.whale.dao.AccountSettingRepository;
import com.buterfleoge.whale.type.entity.AccountInfo;
import com.buterfleoge.whale.type.entity.AccountSetting;
import com.buterfleoge.whale.type.enums.Gender;
import com.buterfleoge.whale.type.protocol.wx.WxAccessTokenResponse;
import com.buterfleoge.whale.type.protocol.wx.WxUserinfoResponse;

/**
 *
 * @author xiezhenzong
 *
 */
@RequestMapping("/wx")
public class WxController {

    @Value("${wx.token}")
    private String wxToken;

    @Value("${{wx.login.callback}}")
    private String wxLoginCallback;

    @Autowired
    private WxBiz wxBiz;

    @Autowired
    private AccountSettingRepository accountSettingRepository;

    @Autowired
    private AccountInfoRepository accountInfoRepository;

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
        String sessionState = getState(request);
        String code = request.getParameter("code");
        String wxState = request.getParameter("state");
        if (StringUtils.isEmpty(code) || sessionState == null || !sessionState.equals(wxState)) {

        }

        WxAccessTokenResponse accessToken = wxBiz.getAccessToken(code);

        WxUserinfoResponse userinfoResponse = wxBiz.getUserinfo(accessToken.getAccess_token(), accessToken.getOpenid());

        String wxid = userinfoResponse.getUnionid();

        AccountInfo info = null;
        AccountSetting setting = accountSettingRepository.findByWxid(wxid);

        if (setting == null) { // 创建账户
            info = new AccountInfo();
            info.setAddTime(System.currentTimeMillis());

            info = accountInfoRepository.save(info);

            setting = new AccountSetting();
            setting.setAccountid(info.getAccountid());
            setting.setWxid(userinfoResponse.getUnionid());
            setting.setAvatarUrl(userinfoResponse.getHeadimgurl());
            setting.setGender(Gender.FEMALE);
            setting.setNickname(userinfoResponse.getNickname());
            setting.setWxname(userinfoResponse.getNickname());

            setting = accountSettingRepository.save(setting);
        } else {
            info = accountInfoRepository.findByAccountid(setting.getAccountid());
        }

        cacheTemplate.opsForValue()
                .set(CacheKey.WX_ACCESS_TOKEN_PREFIX + DefaultValue.SEPARATOR + setting.getAccountid(), accessToken);

        Cookie cookie = new Cookie(CookieKey.ACCOUNTID, String.valueOf(setting.getAccountid()));

        response.addCookie(cookie);

        cookie = new Cookie(CookieKey.TOKEN, String.valueOf(DefaultValue.TOKEN + setting.getAccountid()));

        response.addCookie(cookie);

        response.sendRedirect("/account/" + setting.getAccountid());

    }

    private String createState() {
        StringBuilder sb = new StringBuilder(wxToken) //
                .append(DefaultValue.SEPARATOR).append(System.currentTimeMillis()) //
                .append(DefaultValue.SEPARATOR).append(Math.random());
        String stringMd5 = Utils.stringMD5(sb.toString());
        return stringMd5;
    }

    private String createRedirectUri() throws Exception {
        StringBuilder sb = new StringBuilder(wxLoginCallback).append("/wx/callback");
        return URLEncoder.encode(sb.toString(), "UTF-8");
    }

    private String getState(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        String state = (String) session.getAttribute(SessionKey.WX_LOGIN_STATE);
        return StringUtils.hasText(state) ? state : null;
    }

}
