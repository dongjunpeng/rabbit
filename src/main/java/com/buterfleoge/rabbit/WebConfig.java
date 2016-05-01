package com.buterfleoge.rabbit;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.buterfleoge.rabbit.interceptor.AccountInterceptor;
import com.buterfleoge.rabbit.interceptor.LoginInterceptor;

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
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        for (Entry<String, String> entry : viewMap.entrySet()) {
            registry.addViewController(entry.getKey()).setViewName(entry.getValue());
        }
    }

    @Bean(name = "loginInterceptor")
    public HandlerInterceptor getLoginInterceptor() {
        return new LoginInterceptor();
    }

    @Bean(name = "accountInterceptor")
    public HandlerInterceptor getAccountInterceptor() {
        return new AccountInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(getLoginInterceptor());
        registry.addInterceptor(getAccountInterceptor());
    }

}
