package com.buterfleoge.rabbit.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class IndexController implements InitializingBean {

    public static final String HOT_SEPARATOR = ",";

    @Autowired
    private TravelBiz travelBiz;

    @Value("${route.hot}")
    private String hotRouteids;

    private List<Long> hotRouteidSet = new ArrayList<Long>(4);

    @Override
    public void afterPropertiesSet() throws Exception {
        List<String> routeidList = Arrays.asList(hotRouteids.split(HOT_SEPARATOR));
        for (String routeid : routeidList) {
            hotRouteidSet.add(Long.parseLong(routeid));
        }
    }

    @ResponseBody
    @RequestMapping(value = "/hot", method = RequestMethod.GET)
    public GetRouteResponse getHotRoute(GetRouteRequest request) throws Exception {
        GetRouteResponse response = new GetRouteResponse();
        travelBiz.getRoute(request, response);
        return response;
    }

}
