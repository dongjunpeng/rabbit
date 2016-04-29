/**
 * 
 */
package com.buterfleoge.rabbit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.buterfleoge.whale.biz.travel.TravelBiz;
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
public class TravelController {

    @Autowired
    private TravelBiz travelBiz;

    @ResponseBody
    @RequestMapping(value = "/route", method = RequestMethod.GET)
    public GetRouteResponse getRoute(GetRouteRequest request) throws Exception {
        GetRouteResponse response = new GetRouteResponse();
        travelBiz.getRoute(request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/group", method = RequestMethod.GET)
    public GetGroupResponse getGroup(GetGroupRequest request) throws Exception {
        GetGroupResponse response = new GetGroupResponse();
        travelBiz.getGroup(request, response);
        return response;
    }
}
