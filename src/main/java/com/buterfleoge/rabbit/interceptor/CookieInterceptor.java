package com.buterfleoge.rabbit.interceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import com.buterfleoge.rabbit.WebConfig;
import com.buterfleoge.whale.AesEncryption;
import com.buterfleoge.whale.Constants.CookieKey;
import com.buterfleoge.whale.Constants.SessionKey;
import com.buterfleoge.whale.dao.AccountInfoRepository;
import com.buterfleoge.whale.dao.AccountSettingRepository;
import com.buterfleoge.whale.type.entity.AccountInfo;
import com.buterfleoge.whale.type.entity.AccountSetting;

/**
 *
 * @author xiezhenzong
 *
 */
public class CookieInterceptor extends RabbitInterceptor {

    @Autowired
    private AccountInfoRepository accountInfoRepository;

    @Autowired
    private AccountSettingRepository accountSettingRepository;

    @Autowired
    private AesEncryption aesEncryption;

    @Override
    protected boolean shouldPreHandle(String path) {
        return true;
    }

    @Override
    protected boolean preHandle(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Object accountBasicInfo = request.getSession().getAttribute(SessionKey.ACCOUNT_BASIC_INFO);
        if (accountBasicInfo == null) {
            Cookie[] cookies = request.getCookies();
            Long accountid = getAccountidFromCookie(cookies);
            String token = getTokenFromCookies(cookies);
            if (accountid != null && token != null && accountid.equals(WebConfig.getAccountidFromToken(token))) {
                try {
                    AccountInfo info = accountInfoRepository.findOne(accountid);
                    if (info != null) {
                        AccountSetting setting = accountSettingRepository.findOne(accountid);
                        WebConfig.addBasicInfoToSession(info, setting, request.getSession());
                    } else {
                        LOG.error("accountid in cookie is invalid, accountid: " + accountid + ", token: " + token);
                    }
                } catch (Exception e) {
                    LOG.error("query account in db failed", e);
                }
            }
        }
        return true;
    }

    protected Long getAccountidFromCookie(Cookie[] cookies) {
        String accountid = WebConfig.getValueFromCookies(cookies, CookieKey.ACCOUNTID);
        try {
            return accountid != null ? Long.parseLong(accountid) : null;
        } catch (Exception e) {
            LOG.error("parse accountid in cookie failed, accountid: " + accountid);
            return null;
        }
    }

    private String getTokenFromCookies(Cookie[] cookies) {
        String encryToken = WebConfig.getValueFromCookies(cookies, CookieKey.TOKEN);
        try {
            return encryToken != null ? aesEncryption.decrypt(encryToken) : null;
        } catch (Exception e) {
            LOG.error("decrypt token failed, token: " + encryToken, e);
            return null;
        }
    }

}
