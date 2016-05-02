package com.buterfleoge.rabbit;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * spring mvc config
 *
 * @author xiezhenzong
 *
 */
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    private static final Map<String, String> viewMap = new HashMap<String, String>();

    static {
        viewMap.put("/register", "register");
        viewMap.put("/", "forward:/index.html");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        for (Entry<String, String> entry : viewMap.entrySet()) {
            registry.addViewController(entry.getKey()).setViewName(entry.getValue());
        }
    }

    // @Bean(name = "wxLoginInterceptor")
    // public HandlerInterceptor getWxLoginInterceptor() {
    // return new WxLoginInterceptor();
    // }
    //
    // @Bean(name = "loginStateInterceptor")
    // public HandlerInterceptor getLoginStateInterceptor() {
    // return new LoginStateInterceptor();
    // }
    //
    // @Bean(name = "accountInterceptor")
    // public HandlerInterceptor getAccountInterceptor() {
    // return new AccountInterceptor();
    // }
    //
    // @Override
    // public void addInterceptors(InterceptorRegistry registry) {
    // registry.addInterceptor(getWxLoginInterceptor());
    // registry.addInterceptor(getLoginStateInterceptor());
    // registry.addInterceptor(getAccountInterceptor());
    // }

}
