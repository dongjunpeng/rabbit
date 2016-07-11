package com.buterfleoge.rabbit.controller;

import java.net.URLEncoder;
import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.buterfleoge.rabbit.WebConfig;
import com.buterfleoge.whale.Constants.CookieKey;
import com.buterfleoge.whale.Constants.DefaultValue;
import com.buterfleoge.whale.RsaEncryption;
import com.buterfleoge.whale.Utils;
import com.buterfleoge.whale.biz.account.WxBiz;
import com.buterfleoge.whale.dao.AccountInfoRepository;
import com.buterfleoge.whale.dao.AccountSettingRepository;
import com.buterfleoge.whale.type.AccountStatus;
import com.buterfleoge.whale.type.Gender;
import com.buterfleoge.whale.type.IdType;
import com.buterfleoge.whale.type.entity.AccountInfo;
import com.buterfleoge.whale.type.entity.AccountSetting;
import com.buterfleoge.whale.type.protocol.Request;
import com.buterfleoge.whale.type.protocol.wx.WxAccessTokenResponse;
import com.buterfleoge.whale.type.protocol.wx.WxUserinfoResponse;

/**
 *
 * @author xiezhenzong
 *
 */

@Controller
@RequestMapping("/wx")
public class WxController {

    private static final Logger LOG = LoggerFactory.getLogger(WxController.class);

    @Value("${wx.login.callback}")
    private String wxLoginCallback;

    @Autowired
    private WxBiz wxBiz;

    @Resource(name = "cacheTemplate")
    private ValueOperations<String, Object> operations;

    @Autowired
    private AccountSettingRepository accountSettingRepository;

    @Autowired
    private AccountInfoRepository accountInfoRepository;

    @Autowired
    private RsaEncryption rsaEncryption;

    @RequestMapping(value = "/login")
    public void wxLogin(Request req, HttpServletRequest request, HttpServletResponse httpResponse) throws Exception {
        String state = createState();
        String redirectUri = createRedirectUri();
        String wxLoginUri = wxBiz.getLoginUri(state, redirectUri);
        setState(state);
        httpResponse.sendRedirect(wxLoginUri);
    }

    @RequestMapping(value = "/callback")
    public String wxCallback(Request req, HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession();
        String code = request.getParameter("code");
        if (StringUtils.isEmpty(code) || getState(request.getParameter("state")) == null) {
            return WebConfig.REDIRECT_WXFAILED;
        }
        WxAccessTokenResponse accessToken = wxBiz.getAccessToken(code);
        if (accessToken == null || accessToken.getErrcode() != null) {
            return WebConfig.REDIRECT_WXFAILED;
        }
        WxUserinfoResponse userinfoResponse = wxBiz.getUserinfo(accessToken.getAccess_token(), accessToken.getOpenid());
        if (userinfoResponse == null || userinfoResponse.getErrcode() != null) {
            return WebConfig.REDIRECT_WXFAILED;
        }
        AccountSetting setting = null;
        try {
            setting = accountSettingRepository.findByWxid(userinfoResponse.getUnionid());
        } catch (Exception e) {
            LOG.error("find account setting failed, wxid: " + userinfoResponse.getUnionid(), e);
            return WebConfig.REDIRECT_WXFAILED;
        }
        AccountInfo info = null;
        String redirectPage = null;
        if (setting != null) {
            try {
                info = accountInfoRepository.findOne(setting.getAccountid());
                redirectPage = WebConfig.getAccountHomePage(setting.getAccountid());
            } catch (Exception e) {
                LOG.error("find account info failed, accountid: " + setting.getAccountid(), e);
                return WebConfig.REDIRECT_WXFAILED;
            }
        } else {
            info = createAccountInfo();
            setting = createAccountSetting(userinfoResponse, info);
            redirectPage = WebConfig.getAccountHomePage(info.getAccountid()) + "/info";
        }

        Long accountid = info.getAccountid();
        String token = WebConfig.createToken(accountid);
        String encryToken = new String(rsaEncryption.encrypt(token));
        if (StringUtils.isEmpty(encryToken)) {
            return WebConfig.REDIRECT_WXFAILED;
        }
        WebConfig.addBasicInfoToSession(info, setting, session);
        response.addCookie(createCookie(CookieKey.ACCOUNTID, accountid.toString()));
        response.addCookie(createCookie(CookieKey.TOKEN, encryToken));
        addAccessTokenToCache(accountid, accessToken);
        return "redirect:" + redirectPage;
    }

    private static String createState() {
        StringBuilder sb = new StringBuilder(DefaultValue.TOKEN) //
                .append(DefaultValue.SEPARATOR).append(System.currentTimeMillis()) //
                .append(DefaultValue.SEPARATOR).append(Math.random());
        return Utils.stringMD5(sb.toString());
    }

    private String createRedirectUri() throws Exception {
        StringBuilder sb = new StringBuilder(wxLoginCallback).append("/wx/callback");
        return URLEncoder.encode(sb.toString(), "UTF-8");
    }

    private void setState(String state) {
        try {
            operations.set(WebConfig.getWxLoginStateKey(state), state);
        } catch (Exception e) {
            LOG.error("set state to cache failed, state: " + state, e);
        }
    }

    private String getState(String state) {
        try {
            return (String) operations.get(WebConfig.getWxLoginStateKey(state));
        } catch (Exception e) {
            LOG.error("get wx login state from cache failed, state: " + state, e);
            return null;
        }
    }

    protected AccountInfo createAccountInfo() throws Exception {
        AccountInfo info = new AccountInfo();
        info.setStatus(AccountStatus.WAIT_COMPLETE_INFO);
        info.setIdType(IdType.IDENTIFICATION);
        info.setAddTime(new Date());
        info.setModTime(info.getAddTime());

        try {
            return accountInfoRepository.save(info);
        } catch (Exception e) {
            LOG.error("insert account info failed, info: " + info, e);
            throw e;
        }
    }

    protected AccountSetting createAccountSetting(WxUserinfoResponse userinfoResponse, AccountInfo info)
            throws Exception {
        AccountSetting setting = new AccountSetting();
        setting.setAccountid(info.getAccountid());
        setting.setNickname(userinfoResponse.getNickname());
        setting.setBirthday(new Date());
        setting.setWxname(userinfoResponse.getNickname());
        setting.setWxid(userinfoResponse.getUnionid());
        setting.setAvatarUrl(userinfoResponse.getHeadimgurl());
        setting.setGender(getGender(userinfoResponse.getSex()));
        setting.setModTime(info.getAddTime());

        try {
            return accountSettingRepository.save(setting);
        } catch (Exception e) {
            LOG.error("insert account setting failed, info: " + info + ", setting: " + setting, e);
            throw e;
        }
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

    private Cookie createCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(DefaultValue.COOKIE_EXPIRY);
        return cookie;
    }

    private void addAccessTokenToCache(Long accountid, WxAccessTokenResponse accessToken) {
        try {
            String cacheKey = WebConfig.getAccessTokenKey(accountid);
            operations.set(cacheKey, accessToken);
        } catch (Exception e) {
            LOG.error("add access token to cache failed, accessToken: " + accessToken, e);
        }
    }

}
