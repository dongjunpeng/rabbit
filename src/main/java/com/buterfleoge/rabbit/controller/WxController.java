package com.buterfleoge.rabbit.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.buterfleoge.rabbit.WebConfig;
import com.buterfleoge.rabbit.process.LoginProcess;
import com.buterfleoge.whale.AesEncryption;
import com.buterfleoge.whale.Constants.CacheKey;
import com.buterfleoge.whale.Constants.CookieKey;
import com.buterfleoge.whale.Constants.DefaultValue;
import com.buterfleoge.whale.Utils;
import com.buterfleoge.whale.service.WeixinWebService;
import com.buterfleoge.whale.service.weixin.protocol.WxLoginScope;
import com.buterfleoge.whale.type.entity.AccountInfo;
import com.buterfleoge.whale.type.protocol.Request;

/**
 *
 * @author xiezhenzong
 *
 */
@Controller
@RequestMapping("/wx")
public class WxController extends RabbitController implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(WxController.class);

    private static final String WX_LOGING_CALLBACK = "<!DOCTYPE html><html><head>"
            + "<script>try{console.log(self.opener); self.opener.reloadAccountBasicInfo();"
            + "self.opener.location.reload();self.close();}catch(e){console.log(e);}</script></head><body></body></html>";

    @Value("${wx.login.callback}")
    private String wxLoginCallback;

    @Value("${wx.login.userinfo.callback}")
    private String userinfoCallback;

    @Value("${wx.cgi-bin.token}")
    private String wxCgibinToken;

    @Autowired
    private LoginProcess loginProcess;

    @Autowired
    @Resource(name = "weixinWebService")
    private WeixinWebService weixinWebService;

    @Autowired
    @Resource(name = "weixinCgibinService")
    private WeixinWebService weixinCgibinService;

    @Autowired
    @Resource(name = "cacheTemplate")
    private ValueOperations<String, Object> operations;

    @Autowired
    private AesEncryption aesEncryption;

    @Override
    public void afterPropertiesSet() throws Exception {
        wxLoginCallback = URLEncoder.encode(wxLoginCallback, "UTF-8");
    }

    @RequestMapping(value = "/login")
    public void wxLogin(Request req, HttpServletRequest request, HttpServletResponse httpResponse) throws Exception {
        String state = createState();
        String wxLoginUri = weixinWebService.getLoginUri(state, wxLoginCallback, WxLoginScope.SNSAPI_LOGIN);
        setState(state);
        httpResponse.sendRedirect(wxLoginUri);
    }

    @RequestMapping(value = "/callback")
    public void wxCallback(Request req, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String code = request.getParameter("code");
        if (StringUtils.hasText(code) && getState(request.getParameter("state")) != null) {
            AccountInfo accountInfo = loginProcess.weixinWebLogin(code);
            Long accountid = accountInfo.getAccountid();
            String encryToken = new String(aesEncryption.encrypt(WebConfig.createToken(accountid)));
            if (StringUtils.hasText(encryToken)) {
                addBasicInfoToSession(accountInfo);
                response.addCookie(WebConfig.createCookie(CookieKey.ACCOUNTID, accountid.toString()));
                response.addCookie(WebConfig.createCookie(CookieKey.TOKEN, encryToken));
            }
        }
        response.setHeader("Content-Type", "text/html;charset=UTF-8");
        response.getWriter().write(WX_LOGING_CALLBACK);
    }

    @RequestMapping(value = "/message", method = RequestMethod.GET)
    public void wxMessageGet(Request req, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String[]> parameterMap = request.getParameterMap();
        try {
            String signature = getParameter(parameterMap, "signature");
            String timestamp = getParameter(parameterMap, "timestamp");
            String nonce = getParameter(parameterMap, "nonce");
            String echostr = getParameter(parameterMap, "echostr");
            String cryptStr = getCryptStr(wxCgibinToken, timestamp, nonce);
            if (signature.equals(cryptStr)) {
                response.getWriter().write(echostr);
            } else {
                response.getWriter().write("failed");
            }
        } catch (Exception e) {
            LOG.error("valid failed, reqid: " + req.getReqid(), e);
            response.getWriter().write("failed");
        }
    }

    @RequestMapping(value = "/message", method = RequestMethod.POST)
    public void wxMessagePost(Request req, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.getWriter().write("你请求这个干啥，别搞事！！！");
    }

    @RequestMapping(value = "/wap/callback/base")
    public String wxWapBaseCallback(Request req, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String code = request.getParameter("code");
        if (StringUtils.hasText(code) && getState(request.getParameter("state")) != null) {
            AccountInfo accountInfo = loginProcess.weixinWapBaseLogin(code);
            if (accountInfo != null) {
                addBasicInfoToSession(accountInfo);
                return "redirect:" + getRedirectUrl(request);
            } else {
                // 跳转到手动授权链接
                String state = WxController.createState();
                String wxLoginUri = weixinCgibinService.getLoginUri(state, createCallback(request), WxLoginScope.SNSAPI_USERINFO);
                setState(state);
                return "redirect:" + wxLoginUri;
            }
        }
        return "redirect:/syserror";
    }

    @RequestMapping(value = "/wap/callback/userinfo")
    public String wxWapUserinfoCallback(Request req, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String code = request.getParameter("code");
        if (StringUtils.hasText(code) && getState(request.getParameter("state")) != null) {
            AccountInfo accountInfo = loginProcess.weixinWapUserInfoLogin(code);
            addBasicInfoToSession(accountInfo);
            String redirect = request.getParameter("redirect");
            return "redirect:" + redirect;
        }
        return "redirect:/syserror";
    }

    public static String createState() {
        StringBuilder sb = new StringBuilder(DefaultValue.TOKEN) //
                .append(DefaultValue.SEPARATOR).append(System.currentTimeMillis()) //
                .append(DefaultValue.SEPARATOR).append(Math.random());
        return Utils.stringMD5(sb.toString());
    }

    private void setState(String state) {
        try {
            operations.set(getWxLoginStateKey(state), state);
        } catch (Exception e) {
            LOG.error("set state to cache failed, state: " + state, e);
        }
    }

    private String getState(String state) {
        try {
            return (String) operations.get(getWxLoginStateKey(state));
        } catch (Exception e) {
            LOG.error("get wx login state from cache failed, state: " + state, e);
            return null;
        }
    }

    private String getWxLoginStateKey(String state) {
        return CacheKey.WX_LOGIN_STATE_PREFIX + DefaultValue.SEPARATOR + state;
    }

    private String getParameter(Map<String, String[]> parameterMap, String key) {
        String[] parameters = parameterMap.get(key);
        System.out.println(Arrays.toString(parameters));
        if (parameters == null) {
            throw new IllegalArgumentException("Can't find this parameter in query string: " + parameterMap + ", by key: " + key);
        }
        return parameters[0];
    }

    private String getCryptStr(String token, String timestamp, String nonce) {
        List<String> parameters = Arrays.asList(token, timestamp, nonce);
        Collections.sort(parameters);
        String string = parameters.get(0) + parameters.get(1) + parameters.get(2);
        return DigestUtils.sha1Hex(string);
    }

    private String createCallback(HttpServletRequest request) throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder(userinfoCallback);
        builder.append("?redirect=").append(request.getQueryString() == null ? "/" : request.getParameter("redirect"));
        return URLEncoder.encode(builder.toString(), "UTF-8");
    }

    private String getRedirectUrl(HttpServletRequest request) throws UnsupportedEncodingException {
        String redirect = request.getParameter("redirect");
        return URLDecoder.decode(redirect, "UTF-8");
    }

}
