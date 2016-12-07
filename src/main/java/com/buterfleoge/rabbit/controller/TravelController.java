/**
 *
 */
package com.buterfleoge.rabbit.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.buterfleoge.rabbit.WebConfig;
import com.buterfleoge.whale.biz.TravelBiz;
import com.buterfleoge.whale.dao.TravelRouteRepository;
import com.buterfleoge.whale.type.protocol.Request;
import com.buterfleoge.whale.type.protocol.Response;
import com.buterfleoge.whale.type.protocol.travel.GetGroupRequest;
import com.buterfleoge.whale.type.protocol.travel.GetGroupResponse;
import com.buterfleoge.whale.type.protocol.travel.GetRouteRequest;
import com.buterfleoge.whale.type.protocol.travel.GetRouteResponse;

/**
 *
 * 路线发团相关处理
 *
 * @author Brent24
 *
 */
@Controller
@RequestMapping("/travel")
public class TravelController extends RabbitController {

    private static final Logger LOG = LoggerFactory.getLogger(TravelController.class);

    @Autowired
    private TravelBiz travelBiz;

    @Autowired
    private TravelRouteRepository travelRouteRepository;

    @ResponseBody
    @RequestMapping(value = "/route", method = RequestMethod.GET)
    public Response getRoute(GetRouteRequest request, HttpServletRequest req) throws Exception {
        request.setFromWx(isWeixinUserAgent(req));
        GetRouteResponse response = new GetRouteResponse();
        travelBiz.getRoute(request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/group", method = RequestMethod.GET)
    public Response getGroup(@Valid GetGroupRequest request) throws Exception {
        GetGroupResponse response = new GetGroupResponse();
        travelBiz.getGroups(request, response);
        return response;
    }

    @RequestMapping(value = "/{travelid}", method = RequestMethod.GET)
    public String getTravelPage(@PathVariable Long travelid, Request request, HttpServletRequest req) {
        if (travelid > 0) {
            try {
                if (travelRouteRepository.exists(travelid)) {
                    return isWeixinUserAgent(req) ? "wtravel" : "travel";
                }
            } catch (Exception e) {
                LOG.error("find travel failed, travelid: " + travelid + ", reqid: " + request.getReqid(), e);
            }
        }
        return WebConfig.getNotfoundPage(req);
    }

}
