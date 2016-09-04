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
        return HttpStatus.NOT_FOUND.equals(status) ? new ModelAndView("notfound.html", model) : new ModelAndView("error.html", model);
    }

}
