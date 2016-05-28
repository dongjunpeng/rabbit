package com.buterfleoge.rabbit.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import com.buterfleoge.whale.Constants.SessionKey;
import com.buterfleoge.whale.type.protocol.account.object.AccountBasicInfo;

/**
 *
 * @author xiezhenzong
 *
 */
public abstract class RabbitController {

    @Autowired
    private HttpServletRequest httpServletRequest;

    protected AccountBasicInfo getAccountBasicInfo() {
        HttpSession httpSession = httpServletRequest.getSession();
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

}
