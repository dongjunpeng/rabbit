package com.buterfleoge.rabbit.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.buterfleoge.whale.biz.account.AccountBiz;
import com.buterfleoge.whale.type.protocol.Request;
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
public class AccountController extends RabbitController {

    private static final Logger LOG = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private AccountBiz accountBiz;

    @ResponseBody
    @RequestMapping(value = "/basicinfo", method = RequestMethod.GET)
    public GetBasicInfoResponse getBasicInfo(GetBasicInfoRequest request, HttpServletRequest httpServletRequest)
            throws Exception {
        GetBasicInfoResponse response = new GetBasicInfoResponse();
        AccountBasicInfo basicInfo = getAccountBasicInfo();
        if (basicInfo == null) {
            return response;
        }
        response.setAccountInfo(basicInfo.getAccountInfo());
        response.setAccountSetting(basicInfo.getAccountSetting());
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/basicinfo", method = RequestMethod.POST)
    public Response updateBasicInfo(PostBasicInfoRequest request) throws Exception {
        Response response = new Response();
        accountBiz.updateBasicInfo(requireAccountid(), request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/contacts", method = RequestMethod.GET)
    public GetContactsResponse getContacts(GetContactsRequest request) throws Exception {
        GetContactsResponse response = new GetContactsResponse();
        accountBiz.getContacts(requireAccountid(), request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/contacts", method = RequestMethod.POST)
    public Response postContacts(PostContactsRequest request) throws Exception {
        PostContactsResponse response = new PostContactsResponse();
        accountBiz.postContacts(requireAccountid(), request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/contacts", method = RequestMethod.DELETE)
    public Response deleteContacts(DeleteContactsRequest request) throws Exception {
        Response response = new Response();
        accountBiz.deleteContacts(requireAccountid(), request, response);
        return response;
    }

    @RequestMapping(value = "/{accountid}", method = RequestMethod.GET)
    public String getAccountPage(@PathVariable Long accountid, Request request) throws Exception {
        if (requireAccountid().equals(accountid)) {
            return "account";
        } else {
            LOG.warn("No auth for get homepage of accountid: " + accountid + ", reqid: " + request.getReqid());
            return "redirect:notauth";
        }
    }

    @RequestMapping(value = "/{accountid}/*", method = RequestMethod.GET)
    public String getSubAccountPage(@PathVariable Long accountid, Request request) throws Exception {
        if (requireAccountid().equals(accountid)) {
            return "account";
        } else {
            LOG.warn("No auth for get homepage of accountid: " + accountid + ", reqid: " + request.getReqid());
            return "redirect:notauth";
        }
    }

}
