package com.buterfleoge.rabbit.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.buterfleoge.rabbit.WebConfig;
import com.buterfleoge.whale.dao.TravelRouteRepository;
import com.buterfleoge.whale.type.protocol.Request;

/**
 *
 * @author xiezhenzong
 *
 */
@Controller
@RequestMapping("/")
public class HomeController extends RabbitController {

    private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private TravelRouteRepository travelRouteRepository;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String getHotRoute(Request request, HttpServletRequest req) throws Exception {
        return isWeixinUserAgent(req) ? "wap" : "index";
    }

    @RequestMapping(value = "/wproduct/{travelid}", method = RequestMethod.GET)
    public String getHotRoute(@PathVariable Long travelid, Request request, HttpServletRequest req) throws Exception {
        if (travelid > 0) {
            try {
                if (travelRouteRepository.exists(travelid) && isWeixinUserAgent(req)) {
                    return "wproduct";
                }
            } catch (Exception e) {
                LOG.error("find travel failed, travelid: " + travelid + ", reqid: " + request.getReqid(), e);
            }
        }
        return WebConfig.getNotfoundPage(req);
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
