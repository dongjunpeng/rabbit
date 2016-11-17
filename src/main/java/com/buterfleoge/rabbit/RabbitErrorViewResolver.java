package com.buterfleoge.rabbit;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.web.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author xiezhenzong
 *
 */
@Component
public class RabbitErrorViewResolver implements ErrorViewResolver {

    @Override
    public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
        if (HttpStatus.NOT_FOUND.equals(status)) {
            return new ModelAndView(WebConfig.getNotfoundPage(request), model);
        } else if (HttpStatus.UNAUTHORIZED.equals(status)) {
            return new ModelAndView(WebConfig.getNotauthPage(request), model);
        } else {
            return new ModelAndView(WebConfig.getSyserrorPage(request), model);
        }
    }

}
