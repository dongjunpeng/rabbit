package com.buterfleoge.rabbit.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.buterfleoge.whale.biz.travel.TravelBiz;
import com.buterfleoge.whale.type.protocol.travel.GetRouteRequest;
import com.buterfleoge.whale.type.protocol.travel.GetRouteResponse;

/**
 *
 * @author xiezhenzong
 *
 */
@Controller
@RequestMapping("/index")
public class IndexController {

    @Autowired
    private TravelBiz travelBiz;

    private List<Long> hotRouteidSet = new ArrayList<Long>(4);

    {
        hotRouteidSet.add(1L);
        hotRouteidSet.add(2L);
        hotRouteidSet.add(3L);
        hotRouteidSet.add(4L);
    }

    @ResponseBody
    @RequestMapping(value = "/hot", method = RequestMethod.GET)
    public GetRouteResponse getHotRoute(GetRouteRequest request) throws Exception {
        request.setRouteids(hotRouteidSet);
        GetRouteResponse response = new GetRouteResponse();
        travelBiz.getRoute(request, response);
        return response;
    }

}
