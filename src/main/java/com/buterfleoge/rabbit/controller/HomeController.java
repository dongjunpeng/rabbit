package com.buterfleoge.rabbit.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.buterfleoge.rabbit.WebConfig;
import com.buterfleoge.whale.Constants.Status;
import com.buterfleoge.whale.dao.ActivityRepository;
import com.buterfleoge.whale.dao.TravelRouteRepository;
import com.buterfleoge.whale.type.protocol.GetActivityListResponse;
import com.buterfleoge.whale.type.protocol.Request;
import com.buterfleoge.whale.type.protocol.Response;

/**
 *
 * @author xiezhenzong
 *
 */
@Controller
@RequestMapping("/")
public class HomeController extends RabbitController {

    private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);

    private static final Map<Long, String> PC_ACTIVITY_PAGE = new HashMap<Long, String>();
    private static final Map<Long, String> WAP_ACTIVITY_PAGE = new HashMap<Long, String>();

    static {
        PC_ACTIVITY_PAGE.put(Long.valueOf(1L), "wnewactivity");
        WAP_ACTIVITY_PAGE.put(Long.valueOf(1L), "wnewactivity");

    }

    @Autowired
    private TravelRouteRepository travelRouteRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String getHotRoute(Request request, HttpServletRequest req) throws Exception {
        return isWeixinUserAgent(req) ? "wap" : "index";
    }

    @RequestMapping(value = "/activities", method = RequestMethod.GET)
    public String getActivities(Request request, HttpServletRequest req) throws Exception {
        return isWeixinUserAgent(req) ? "wactivities" : "wactivities";
    }

    @ResponseBody
    @RequestMapping(value = "/activity/list", method = RequestMethod.GET)
    public Response getActivityList(Request request, HttpServletRequest req) throws Exception {
        GetActivityListResponse response = new GetActivityListResponse();
        try {
            Date now = new Date();
            response.setActivities(activityRepository.findOpenActivity(now, now));
        } catch (Exception e) {
            LOG.error("get activities failed", e);
            response.setStatus(Status.DB_ERROR);
        }
        return response;
    }

    @RequestMapping(value = "/activity/{activityid}", method = RequestMethod.GET)
    public String getActivityList(@PathVariable Long activityid, Request request, HttpServletRequest req)
            throws Exception {
        if (activityid > 0) {
            try {
                if (activityRepository.exists(activityid)) {
                    return isWeixinUserAgent(req) ? WAP_ACTIVITY_PAGE.get(activityid)
                            : PC_ACTIVITY_PAGE.get(activityid);
                }
            } catch (Exception e) {
                LOG.error("find activity failed, activityid: " + activityid + ", reqid: " + request.getReqid(), e);
            }
        }
        return WebConfig.getNotfoundPage(req);
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
        return WebConfig.getNotauthPage(req);
    }

    @RequestMapping(value = "/notfound", method = RequestMethod.GET)
    public String getNotfound(Request request, HttpServletRequest req) throws Exception {
        return WebConfig.getNotfoundPage(req);
    }

    @RequestMapping(value = "/syserror", method = RequestMethod.GET)
    public String getSyserror(Request request, HttpServletRequest req) throws Exception {
        return WebConfig.getSyserrorPage(req);
    }

}
