package com.buterfleoge.rabbit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;

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
        viewMap.put("/", "forward:/index.html");
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
