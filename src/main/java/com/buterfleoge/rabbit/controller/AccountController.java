package com.buterfleoge.rabbit.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.buterfleoge.rabbit.WebConfig;
import com.buterfleoge.whale.Constants.Status;
import com.buterfleoge.whale.biz.AccountBiz;
import com.buterfleoge.whale.dao.ActivityRepository;
import com.buterfleoge.whale.dao.PostcardRepository;
import com.buterfleoge.whale.type.entity.AccountInfo;
import com.buterfleoge.whale.type.entity.Activity;
import com.buterfleoge.whale.type.entity.Postcard;
import com.buterfleoge.whale.type.protocol.Error;
import com.buterfleoge.whale.type.protocol.PostcardJoinRequest;
import com.buterfleoge.whale.type.protocol.Request;
import com.buterfleoge.whale.type.protocol.Response;
import com.buterfleoge.whale.type.protocol.account.DeleteContactsRequest;
import com.buterfleoge.whale.type.protocol.account.GetBasicInfoResponse;
import com.buterfleoge.whale.type.protocol.account.GetContactsRequest;
import com.buterfleoge.whale.type.protocol.account.GetContactsResponse;
import com.buterfleoge.whale.type.protocol.account.GetCouponsRequest;
import com.buterfleoge.whale.type.protocol.account.GetCouponsResponse;
import com.buterfleoge.whale.type.protocol.account.GetWxShareConfigRequest;
import com.buterfleoge.whale.type.protocol.account.GetWxShareConfigResponse;
import com.buterfleoge.whale.type.protocol.account.PostBasicInfoRequest;
import com.buterfleoge.whale.type.protocol.account.PostContactsRequest;
import com.buterfleoge.whale.type.protocol.account.object.AccountBasicInfo;
import com.buterfleoge.whale.type.protocol.order.ValidateCodeRequest;
import com.buterfleoge.whale.type.protocol.order.ValidateCodeResponse;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private PostcardRepository postcardRepository;

    @ResponseBody
    @RequestMapping(value = "/basicinfo", method = RequestMethod.GET)
    public Response getBasicInfo(Request request, HttpServletRequest httpServletRequest)
            throws Exception {
        GetBasicInfoResponse response = new GetBasicInfoResponse();
        AccountBasicInfo basicInfo = getAccountBasicInfo();
        if (basicInfo != null) {
            response.setAccountBasicInfo(basicInfo);
        }
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/info", method = RequestMethod.POST)
    public Response updateBasicInfo(PostBasicInfoRequest request) throws Exception {
        Long accountid = requireAccountid();
        Response response = new Response();
        AccountInfo accountInfo = accountBiz.updateBasicInfo(accountid, request, response);
        if (accountInfo != null) {
            addBasicInfoToSession(accountInfo);
        }
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/contacts", method = RequestMethod.GET)
    public Response getContacts(GetContactsRequest request) throws Exception {
        GetContactsResponse response = new GetContactsResponse();
        accountBiz.getContacts(requireAccountid(), request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/contacts", method = RequestMethod.POST)
    public Response postContacts(PostContactsRequest request) throws Exception {
        Response response = new Response();
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

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(Request req, HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession httpSession = getHttpSession();
        httpSession.invalidate();
        Cookie[] cookies = request.getCookies();
        if (ArrayUtils.isNotEmpty(cookies)) {
            for (Cookie cookie : cookies) {
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        }
        String referer = request.getHeader("Referer");
        return StringUtils.isEmpty(referer) || referer.startsWith(WebConfig.ACCOUNT_HOME_URL_PREFIX)
                || referer.startsWith(WebConfig.ORDER_URL_PREFIX) ? "redirect:/" : "redirect:" + referer;
    }

    @ResponseBody
    @RequestMapping(value = "/coupons", method = RequestMethod.GET)
    public Response getCoupons(GetCouponsRequest request) throws Exception {
        GetCouponsResponse response = new GetCouponsResponse();
        accountBiz.getCoupons(requireAccountid(), request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/discountcode/validate", method = RequestMethod.GET)
    public Response validateDiscountCode(ValidateCodeRequest request) throws Exception {
        ValidateCodeResponse response = new ValidateCodeResponse();
        accountBiz.validateDiscountCode(requireAccountid(), request, response);
        return response;
    }

    @RequestMapping(value = "/wcontact", method = RequestMethod.GET)
    public String getWapContact(Request request, HttpServletRequest httpServletRequest) throws Exception {
        return isWeixinUserAgent(httpServletRequest) ? "wcontact" : WebConfig.getNotfoundPage(httpServletRequest);
    }

    @RequestMapping(value = "/wcoupon", method = RequestMethod.GET)
    public String getWapDiscount(Request request, HttpServletRequest httpServletRequest) throws Exception {
        return isWeixinUserAgent(httpServletRequest) ? "wcoupon" : WebConfig.getNotfoundPage(httpServletRequest);
    }

    @ResponseBody
    @RequestMapping(value = "/wxshareconfig", method = RequestMethod.GET)
    public Response getWxShareConfig(GetWxShareConfigRequest request) throws Exception {
        GetWxShareConfigResponse response = new GetWxShareConfigResponse();
        accountBiz.getWxShareConfig(requireAccountid(), request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/postcard/join", method = RequestMethod.POST)
    public Response getWxShareConfig(PostcardJoinRequest request) throws Exception {
        Response response = new Response();
        Long activityid = request.getActivityid();
        Long accountid = requireAccountid();
        int count = 0;
        if (activityid == null || accountid < 0) {
            return response;
        }
        try {
            if (postcardRepository.countByActivityidAndAccountid(activityid, accountid) > 0) {
                return response;
            }
            if (StringUtils.isEmpty(request.getAddress())) {
                response.setStatus(Status.BIZ_ERROR);
                response.addError(new Error("地址为空"));
                return response;
            }
            Activity activity = activityRepository.findOne(activityid);
            if (activity == null) {
                response.setStatus(Status.BIZ_ERROR);
                response.addError(new Error("活动不存在"));
                return response;
            }
            ObjectNode param = activity.getParam();
            count = param.get("count").asInt();
            if (count > 0) {
                param.set("count", new IntNode(--count));
            }
            postcardRepository.save(Postcard.create(activityid, accountid, request.getAddress()));
            activityRepository.save(activity);
        } catch (Exception e) {
            LOG.error("update postcard activity failed, count: " + count, e);
            response.setStatus(Status.DB_ERROR);
            response.addError(new Error("系统异常"));
        }
        return response;
    }

    @RequestMapping(value = "/{accountid}", method = RequestMethod.GET)
    public String getAccountPage(@PathVariable Long accountid, Request request, HttpServletRequest httpServletRequest) throws Exception {
        if (requireAccountid().equals(accountid)) {
            return isWeixinUserAgent(httpServletRequest) ? "redirect:/wap" : "account";
        } else {
            LOG.warn("No auth for get homepage of accountid: " + accountid + ", reqid: " + request.getReqid());
            return WebConfig.getNotauthPage(httpServletRequest);
        }
    }

}
