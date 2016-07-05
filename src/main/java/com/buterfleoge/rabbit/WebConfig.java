package com.buterfleoge.rabbit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.buterfleoge.rabbit.interceptor.CookieInterceptor;
import com.buterfleoge.rabbit.interceptor.LoginStateInterceptor;
import com.buterfleoge.rabbit.interceptor.RabbitWebContextInterceptor;
import com.buterfleoge.rabbit.interceptor.WxLoginInterceptor;
import com.buterfleoge.whale.Constants.CacheKey;
import com.buterfleoge.whale.Constants.DefaultValue;
import com.buterfleoge.whale.Constants.SessionKey;
import com.buterfleoge.whale.Constants.Status;
import com.buterfleoge.whale.Utils;
import com.buterfleoge.whale.type.entity.AccountInfo;
import com.buterfleoge.whale.type.entity.AccountSetting;
import com.buterfleoge.whale.type.protocol.account.object.AccountBasicInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * spring mvc config
 *
 * @author xiezhenzong
 *
 */
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    protected static final Logger LOG = LoggerFactory.getLogger(WebConfig.class);

    public static final String ACCOUNT_HOME_URL_PREFIX = "/account";
    public static final String ORDER_URL_PREFIX = "/order";
    public static final String WX_LOGIN_URL = "/wx/login";
    public static final String REDIRECT_NOTAUTH = "redirect:notauth";
    public static final String REDIRECT_WXFAILED = "redirect:/wx/failed";
    public static final String REDIRECT_FAILED = "redirect:systemerror";

    private static final Map<String, String> viewMap = new HashMap<String, String>();

    static {
        viewMap.put("/", "forward:/index.html");
        viewMap.put("/routes", "forward:/routes.html");
        viewMap.put("/activities", "forward:/activities.html");
    }

    private static final Map<String, Integer> FAILED_PATH_STATUS_MAP = new HashMap<String, Integer>();

    static {
        FAILED_PATH_STATUS_MAP.put(REDIRECT_NOTAUTH, Status.AUTH_ERROR);
        FAILED_PATH_STATUS_MAP.put(REDIRECT_WXFAILED, Status.BIZ_ERROR);
        FAILED_PATH_STATUS_MAP.put(REDIRECT_FAILED, Status.SYSTEM_ERROR);
    }

    @Value("${img.host.url}")
    private String imgHostUrl;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        for (Entry<String, String> entry : viewMap.entrySet()) {
            registry.addViewController(entry.getKey()).setViewName(entry.getValue());
        }
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> httpMessageConverter : converters) {
            if (httpMessageConverter instanceof MappingJackson2HttpMessageConverter) {
                ObjectMapper om = ((MappingJackson2HttpMessageConverter) httpMessageConverter).getObjectMapper();
                om.setAnnotationIntrospector(new RabbitJacksonAnnotationIntrospector(imgHostUrl));
            }
        }
    }

    @Bean(name = "rabbitWebContextInterceptor")
    public HandlerInterceptor getRabbitWebContextInterceptor() {
        return new RabbitWebContextInterceptor();
    }

    @Bean(name = "cookieInterceptor")
    public HandlerInterceptor getCookieInterceptor() {
        return new CookieInterceptor();
    }

    @Bean(name = "wxLoginInterceptor")
    public HandlerInterceptor getWxLoginInterceptor() {
        return new WxLoginInterceptor();
    }

    @Bean(name = "loginStateInterceptor")
    public HandlerInterceptor getLoginStateInterceptor() {
        return new LoginStateInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(getRabbitWebContextInterceptor());
        registry.addInterceptor(getCookieInterceptor());
        registry.addInterceptor(getWxLoginInterceptor());
        registry.addInterceptor(getLoginStateInterceptor());
    }

    public static Integer getStatusByFailedPath(String path) {
        return FAILED_PATH_STATUS_MAP.get(path);
    }

    public static String getAccountHomePage(Long accountid) {
        return ACCOUNT_HOME_URL_PREFIX + "/" + accountid;
    }

    public static String getValueFromCookies(Cookie[] cookies, String key) {
        if (ArrayUtils.isEmpty(cookies)) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (key.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public static void addBasicInfoToSession(AccountInfo info, AccountSetting setting, HttpSession session) {
        AccountBasicInfo basicInfo = new AccountBasicInfo();
        basicInfo.setAccountInfo(info);
        basicInfo.setAccountSetting(setting);
        session.setAttribute(SessionKey.ACCOUNT_BASIC_INFO, basicInfo);
    }

    public static String createToken(Long accountid) {
        return Utils.stringMD5(accountid + DefaultValue.SEPARATOR + DefaultValue.TOKEN);
    }

    public static String getAccessTokenKey(Long accountid) {
        return CacheKey.WX_ACCESS_TOKEN_PREFIX + DefaultValue.SEPARATOR + accountid;
    }

    public static String getWxLoginStateKey(String state) {
        return CacheKey.WX_LOGIN_STATE_PREFIX + DefaultValue.SEPARATOR + state;
    }
}
