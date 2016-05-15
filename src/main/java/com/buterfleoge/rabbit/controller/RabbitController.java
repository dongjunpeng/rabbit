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
        HttpSession httpSession = httpServletRequest.getSession(false);
        if (httpSession == null) {
            return null;
        }
        return (AccountBasicInfo) httpSession.getAttribute(SessionKey.ACCOUNT_BASIC_INFO);
    }

}
