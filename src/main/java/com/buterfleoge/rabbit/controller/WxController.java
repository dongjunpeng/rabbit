package com.buterfleoge.rabbit.controller;

import java.net.URLEncoder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.buterfleoge.rabbit.WebConfig;
import com.buterfleoge.rabbit.process.LoginProcess;
import com.buterfleoge.whale.AesEncryption;
import com.buterfleoge.whale.Constants.CacheKey;
import com.buterfleoge.whale.Constants.CookieKey;
import com.buterfleoge.whale.Constants.DefaultValue;
import com.buterfleoge.whale.Utils;
import com.buterfleoge.whale.service.WeixinWebService;
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

    // FIXME：不需要整个页面刷新
    private static final String WX_LOGING_CALLBACK = "<!DOCTYPE html><html><head>"
            + "<script>try{self.opener.location.reload();}catch(e){}self.close();</script></head><body></body></html>";

    @Value("${wx.login.callback}")
    private String wxLoginCallback;

    @Autowired
    private LoginProcess loginProcess;

    @Autowired
    private WeixinWebService weixinWebService;

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
        String wxLoginUri = weixinWebService.getLoginUri(state, wxLoginCallback);
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

    private static String createState() {
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

}
