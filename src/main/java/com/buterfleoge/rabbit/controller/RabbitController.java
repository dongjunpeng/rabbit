package com.buterfleoge.rabbit.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import com.buterfleoge.whale.Constants.SessionKey;
import com.buterfleoge.whale.type.entity.AccountInfo;
import com.buterfleoge.whale.type.protocol.account.object.AccountBasicInfo;

/**
 *
 * @author xiezhenzong
 *
 */
public abstract class RabbitController {

    @Autowired
    private HttpServletRequest httpServletRequest;

    protected HttpSession getHttpSession() {
        return httpServletRequest.getSession();
    }

    protected HttpSession requireHttpSession() {
        return httpServletRequest.getSession(true);
    }

    protected AccountBasicInfo getAccountBasicInfo() {
        HttpSession httpSession = getHttpSession();
        return (AccountBasicInfo) httpSession.getAttribute(SessionKey.ACCOUNT_BASIC_INFO);
    }

    protected AccountBasicInfo requireAccountBasicInfo() {
        AccountBasicInfo accountBasicInfo = getAccountBasicInfo();
        if (accountBasicInfo == null) {
            throw new IllegalStateException("No account basic info in session");
        }
        return accountBasicInfo;
    }

    protected Long requireAccountid() {
        AccountBasicInfo accountBasicInfo = requireAccountBasicInfo();
        return accountBasicInfo.getAccountInfo().getAccountid();
    }

    protected void addBasicInfoToSession(AccountInfo info) {
        AccountBasicInfo basicInfo = new AccountBasicInfo();
        basicInfo.setAccountInfo(info);
        requireHttpSession().setAttribute(SessionKey.ACCOUNT_BASIC_INFO, basicInfo);
    }

}
