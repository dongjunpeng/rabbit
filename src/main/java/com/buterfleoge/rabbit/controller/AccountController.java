package com.buterfleoge.rabbit.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.buterfleoge.whale.Constants.SessionKey;
import com.buterfleoge.whale.biz.account.AccountBiz;
import com.buterfleoge.whale.dao.AccountInfoRepository;
import com.buterfleoge.whale.dao.AccountSettingRepository;
import com.buterfleoge.whale.type.protocol.Response;
import com.buterfleoge.whale.type.protocol.account.DeleteContactsRequest;
import com.buterfleoge.whale.type.protocol.account.GetBasicInfoRequest;
import com.buterfleoge.whale.type.protocol.account.GetBasicInfoResponse;
import com.buterfleoge.whale.type.protocol.account.GetContactsRequest;
import com.buterfleoge.whale.type.protocol.account.GetContactsResponse;
import com.buterfleoge.whale.type.protocol.account.PostBasicInfoRequest;
import com.buterfleoge.whale.type.protocol.account.PostContactsRequest;
import com.buterfleoge.whale.type.protocol.account.PostContactsResponse;
import com.buterfleoge.whale.type.protocol.account.object.AccountBasicInfo;

/**
 * 账户相关处理
 *
 * @author xiezhenzong
 *
 */
@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountBiz accountBiz;

    @Autowired
    private AccountInfoRepository accountInfoRepository;

    @Autowired
    private AccountSettingRepository accountSettingRepository;

    @ResponseBody
    @RequestMapping(value = "/basicinfo", method = RequestMethod.GET)
    public GetBasicInfoResponse getBasicInfo(GetBasicInfoRequest request, HttpServletRequest httpServletRequest)
            throws Exception {
        GetBasicInfoResponse response = new GetBasicInfoResponse();
        HttpSession session = httpServletRequest.getSession(false);
        if (session == null) {
            response.setAccountInfo(accountInfoRepository.findOne(2L));
            response.setAccountSetting(accountSettingRepository.findOne(2L));
            return response;
        }
        AccountBasicInfo basicInfo = (AccountBasicInfo) session.getAttribute(SessionKey.ACCOUNT_BASIC_INFO);
        if (basicInfo == null) {
            return response;
        }
        response.setAccountInfo(basicInfo.getAccountInfo());
        response.setAccountSetting(basicInfo.getAccountSetting());
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/basicinfo", method = RequestMethod.PUT)
    public Response updateBasicInfo(PostBasicInfoRequest request) throws Exception {
        Response response = new Response();
        accountBiz.updateBasicInfo(request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/contacts", method = RequestMethod.GET)
    public GetContactsResponse getContacts(GetContactsRequest request) throws Exception {
        GetContactsResponse response = new GetContactsResponse();
        accountBiz.getContacts(request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/contacts", method = RequestMethod.POST)
    public Response postContacts(PostContactsRequest request) throws Exception {
        PostContactsResponse response = new PostContactsResponse();
        accountBiz.postContacts(request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/contacts", method = RequestMethod.DELETE)
    public Response deleteContacts(DeleteContactsRequest request) throws Exception {
        Response response = new Response();
        accountBiz.deleteContacts(request, response);
        return response;
    }

    @RequestMapping(value = "/**", method = RequestMethod.GET)
    public String getAccountPage() {
        return "account";
    }

}
