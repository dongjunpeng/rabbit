package com.buterfleoge.rabbit.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.buterfleoge.whale.Constants.SessionKey;
import com.buterfleoge.whale.Constants.Status;
import com.buterfleoge.whale.biz.account.AccountBiz;
import com.buterfleoge.whale.dao.AccountSettingRepository;
import com.buterfleoge.whale.type.AccountType;
import com.buterfleoge.whale.type.entity.AccountInfo;
import com.buterfleoge.whale.type.entity.AccountSetting;
import com.buterfleoge.whale.type.protocol.Request;
import com.buterfleoge.whale.type.protocol.Response;
import com.buterfleoge.whale.type.protocol.account.EmailExistRequest;
import com.buterfleoge.whale.type.protocol.account.GetBasicInfoResponse;
import com.buterfleoge.whale.type.protocol.account.LoginRequest;
import com.buterfleoge.whale.type.protocol.account.RegisterRequest;
import com.buterfleoge.whale.type.protocol.account.RegisterResponse;
import com.buterfleoge.whale.type.protocol.account.ValidateEmailRequest;

/**
 * 账户相关处理
 *
 * @author xiezhenzong
 *
 */
@Controller
@RequestMapping("/account")
public class AccountController {

    private static final Logger LOG = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private AccountBiz accountBiz;

    @Autowired
    private AccountSettingRepository accountSettingRepository;

    @Autowired
    private HttpServletRequest httpRequest;

    @ResponseBody
    @RequestMapping(value = "/email", method = RequestMethod.POST)
    public Response checkEmailExist(EmailExistRequest request) throws Exception {
        Response response = new Response();
        accountBiz.isEmailExist(request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public RegisterResponse register(RegisterRequest request) throws Exception {
        RegisterResponse response = new RegisterResponse();
        request.setType(AccountType.USER);
        accountBiz.registerByEmail(request, response);
        if (response.hasError()) {
            return response;
        }
        response.getAccountInfo().setPassword(null);
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(SessionKey.ACCOUNT_BASIC_INF, response.getAccountInfo());
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/basicinfo", method = RequestMethod.GET)
    public GetBasicInfoResponse getBasicInfo(Request request) throws Exception {
        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            return null;
        }

        GetBasicInfoResponse response = new GetBasicInfoResponse();
        Object basicInfo = session.getAttribute(SessionKey.ACCOUNT_BASIC_INF);
        if (basicInfo == null || !(basicInfo instanceof GetBasicInfoResponse)) {
            return response;
        } else {
            GetBasicInfoResponse getBasicInfoResponse = (GetBasicInfoResponse) basicInfo;
            AccountInfo accountInfo = getBasicInfoResponse.getAccountInfo();
            AccountSetting accountSetting = getBasicInfoResponse.getAccountSetting();
            if (accountSetting == null) {
                try {
                    accountSetting = accountSettingRepository.findOne(accountInfo.getAccountid());
                    getBasicInfoResponse.setAccountSetting(accountSetting);
                } catch (Exception e) {
                    LOG.error("find accountSetting failed, accountInfo: " + accountInfo, e);
                    response.setStatus(Status.DB_ERROR);
                    return response;
                }
            }
            response.setLogin(true);
            response.setAccountInfo(accountInfo);
            response.setAccountSetting(accountSetting);
        }
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/validateEmail", method = RequestMethod.GET)
    public Response validateEmail(ValidateEmailRequest request) throws Exception {
        Response response = new Response();
        accountBiz.validateEmail(request, response);
        return response;
    }

    // email登陆
    @ResponseBody
    @RequestMapping("/login")
    public Response login(LoginRequest request) {
        // Response<AccountInfo> response = new Response<AccountInfo>();
        // try {
        // request.getFirstDataItem().setType(AccountType.USER);
        // accountBiz.loginByEmail(request, response);
        // } catch (Exception e) {
        // LOG.error("Login failed", e);
        // response.setStatus(Status.SYSTEM_ERROR);
        // }
        // return response;
        return null;
    }


}
