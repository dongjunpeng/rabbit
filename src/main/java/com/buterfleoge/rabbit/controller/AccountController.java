package com.buterfleoge.rabbit.controller;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.buterfleoge.whale.biz.account.AccountBiz;
import com.buterfleoge.whale.constant.Constants.Status;
import com.buterfleoge.whale.type.AccountType;
import com.buterfleoge.whale.type.entity.AccountInfo;
import com.buterfleoge.whale.type.protocol.Request;
import com.buterfleoge.whale.type.protocol.Response;
import com.buterfleoge.whale.type.protocol.account.EmailExistRequestItem;
import com.buterfleoge.whale.type.protocol.account.LoginRequestItem;
import com.buterfleoge.whale.type.protocol.account.RegisterRequestItem;

/**
 * 账户相关处理
 *
 * @author xiezhenzong
 *
 */
@Controller
@RequestMapping("/account")
public class AccountController {

    private static final Logger LOG = Logger.getLogger(AccountController.class);

    @Autowired
    private AccountBiz accountBiz;

    @ResponseBody
    @RequestMapping(value = "/email", method = RequestMethod.POST)
    public Response<Void> checkEmailExist(Request<EmailExistRequestItem> request) {
        Response<Void> response = new Response<Void>();
        try {
            accountBiz.isEmailExist(request, response);
        } catch (Exception e) {
            LOG.error("Check email exist failed", e);
            response.setStatus(Status.SYSTEM_ERROR);
        }
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public Response<Void> register(Request<RegisterRequestItem> request) {
        Response<Void> response = new Response<Void>();
        try {
            Request<EmailExistRequestItem> emailEmailRequest = createEmailExistRequestFromRegisterRequest(request);
            accountBiz.isEmailExist(emailEmailRequest, response);
            if (response.hasError()) {
                return response;
            }
            RegisterRequestItem requestItem = request.getFirstDataItem();
            requestItem.setType(AccountType.USER);
            accountBiz.registerByEmail(request, response);
        } catch (Exception e) {
            LOG.error("Register failed", e);
            response.setStatus(Status.SYSTEM_ERROR);
        }
        return response;
    }

    /**
     * @param request
     * @return
     */
    protected Request<EmailExistRequestItem>
            createEmailExistRequestFromRegisterRequest(Request<RegisterRequestItem> request) {
        String email = request.getFirstDataItem().getEmail();
        Request<EmailExistRequestItem> emailEmailRequest = new Request<EmailExistRequestItem>();
        emailEmailRequest.setLogid(request.getLogid());
        emailEmailRequest.setGlobalid(request.getGlobalid());
        emailEmailRequest.setData(Arrays.asList(new EmailExistRequestItem(email)));
        return emailEmailRequest;
    }

    // email登陆
    @RequestMapping("/login")
    @ResponseBody
    public Response<AccountInfo> login(Request<LoginRequestItem> request) {
        Response<AccountInfo> response = new Response<AccountInfo>();

        try {
            request.getFirstDataItem().setType(AccountType.USER);

            System.out.println();
        } catch (Exception e) {
            LOG.error("Login failed", e);
            response.setStatus(Status.SYSTEM_ERROR);
        }
        return response;
    }

}
