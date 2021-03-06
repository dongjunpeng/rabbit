package com.buterfleoge.rabbit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.connector.Connector;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.buterfleoge.rabbit.interceptor.CookieInterceptor;
import com.buterfleoge.rabbit.interceptor.LoginStateInterceptor;
import com.buterfleoge.rabbit.interceptor.RabbitWebContextInterceptor;
import com.buterfleoge.rabbit.interceptor.WapInterceptor;
import com.buterfleoge.rabbit.interceptor.WxLoginInterceptor;
import com.buterfleoge.rabbit.view.PdfView;
import com.buterfleoge.whale.Constants.CacheKey;
import com.buterfleoge.whale.Constants.DefaultValue;
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
    public static final String LOGIN_URL = "/login";
    public static final String WX_LOGIN_URL = "/wx/login";
    public static final String NOTAUTH_URL = "/notauth";
    public static final String SYSERROR = "/syserror";

    private static final Map<String, String> viewMap = new HashMap<String, String>();

    static {
        viewMap.put("/routes", "/routes.html");
        viewMap.put(LOGIN_URL, "/login.html");
        viewMap.put("/order/wxpay/result", "/wxpayresult.html");
    }

    @Value("${img.host.url}")
    private String imgHostUrl;

    @Value("${spring.tomcat.ajp.port}")
    int ajpPort;

    @Value("${spring.tomcat.ajp.enabled}")
    boolean tomcatAjpEnabled;

    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
        if (tomcatAjpEnabled) {
            Connector ajpConnector = new Connector("AJP/1.3");
            ajpConnector.setProtocol("AJP/1.3");
            ajpConnector.setPort(ajpPort);
            ajpConnector.setSecure(false);
            ajpConnector.setAllowTrace(false);
            ajpConnector.setScheme("http");
            tomcat.addAdditionalTomcatConnectors(ajpConnector);
        }
        return tomcat;
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

    @Bean(name = "wapInterceptor")
    public HandlerInterceptor getWapInterceptor() {
        return new WapInterceptor();
    }

    @Bean(name = "pdfView")
    public PdfView getContractView() {
        return new PdfView();
    }

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

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(getRabbitWebContextInterceptor());
        registry.addInterceptor(getCookieInterceptor());
        registry.addInterceptor(getWxLoginInterceptor());
        registry.addInterceptor(getLoginStateInterceptor());
        registry.addInterceptor(getWapInterceptor());
    }

    public static String getAccountHomePage(Long accountid) {
        return ACCOUNT_HOME_URL_PREFIX + "/" + accountid;
    }

    public static String getNotauthPage(HttpServletRequest request) {
        return isWeixinUserAgent(request) ? "wnotauth" : "notauth";
    }

    public static String getNotfoundPage(HttpServletRequest request) {
        return isWeixinUserAgent(request) ? "wnotfound" : "notfound";
    }

    public static String getSyserrorPage(HttpServletRequest request) {
        return isWeixinUserAgent(request) ? "wsyserror" : "syserror";
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

    public static Cookie createCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(DefaultValue.COOKIE_EXPIRY);
        return cookie;
    }

    public static String createToken(Long accountid) {
        return accountid + DefaultValue.SEPARATOR + DefaultValue.TOKEN + DefaultValue.SEPARATOR
                + System.currentTimeMillis();
    }

    public static Long getAccountidFromToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        String[] temp = token.split(DefaultValue.SEPARATOR);
        if (temp.length != 3 || DefaultValue.TOKEN.equals(temp[1])) {
            return null;
        }
        try {
            return Long.parseLong(temp[0]);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getAccessTokenKey(Long accountid) {
        return CacheKey.WX_ACCESS_TOKEN_PREFIX + DefaultValue.SEPARATOR + accountid;
    }

    public static String getWapAccessTokenKey(Long accountid) {
        return "m" + DefaultValue.SEPARATOR + CacheKey.WX_ACCESS_TOKEN_PREFIX + DefaultValue.SEPARATOR + accountid;
    }

    public static boolean isWeixinUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null && userAgent.contains("MicroMessenger");
    }

}
