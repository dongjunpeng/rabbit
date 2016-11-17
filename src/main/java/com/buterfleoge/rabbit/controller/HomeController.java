package com.buterfleoge.rabbit.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.buterfleoge.whale.type.protocol.Request;

/**
 *
 * @author xiezhenzong
 *
 */
@Controller
@RequestMapping("/")
public class HomeController extends RabbitController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String getHotRoute(Request request, HttpServletRequest req) throws Exception {
        return isWeixinUserAgent(req) ? "wap" : "index";
    }

    @RequestMapping(value = "/notauth", method = RequestMethod.GET)
    public String getNotauth(Request request, HttpServletRequest req) throws Exception {
        return isWeixinUserAgent(req) ? "wnotauth" : "notauth";
    }

    @RequestMapping(value = "/notfound", method = RequestMethod.GET)
    public String getNotfound(Request request, HttpServletRequest req) throws Exception {
        return isWeixinUserAgent(req) ? "wnotfound" : "notfound";
    }

    @RequestMapping(value = "/syserror", method = RequestMethod.GET)
    public String getSyserror(Request request, HttpServletRequest req) throws Exception {
        return isWeixinUserAgent(req) ? "wsyserror" : "syserror";
    }

}
