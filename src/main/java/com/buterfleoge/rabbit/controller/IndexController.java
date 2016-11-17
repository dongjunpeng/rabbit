package com.buterfleoge.rabbit.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.buterfleoge.rabbit.view.PdfView;
import com.buterfleoge.whale.biz.travel.TravelBiz;
import com.buterfleoge.whale.type.protocol.Response;
import com.buterfleoge.whale.type.protocol.order.GetContractRequest;
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
    public Response getHotRoute(GetRouteRequest request) throws Exception {
        request.setRouteids(hotRouteidSet);
        GetRouteResponse response = new GetRouteResponse();
        travelBiz.getRoute(request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/wap/hot", method = RequestMethod.GET)
    public Response getWapHotRoute(GetRouteRequest request) throws Exception {
        request.setRouteids(Arrays.asList(hotRouteidSet.get(0)));
        GetRouteResponse response = new GetRouteResponse();
        travelBiz.getRoute(request, response);
        return response;
    }

    @RequestMapping(value = "/hxy_secure_notice", method = RequestMethod.GET)
    public ModelAndView getHxySecureNotice(GetContractRequest request) throws Exception {
        ModelAndView modelAndView = new ModelAndView("pdfView");
        modelAndView.addObject(PdfView.PATH_KEY, "classpath:hxy_secure_notice.pdf");
        return modelAndView;
    }

    @RequestMapping(value = "/hxy_signup_notice", method = RequestMethod.GET)
    public ModelAndView getHxyXXXNotice(GetContractRequest request) throws Exception {
        ModelAndView modelAndView = new ModelAndView("pdfView");
        modelAndView.addObject(PdfView.PATH_KEY, "classpath:hxy_signup_notice.pdf");
        return modelAndView;
    }

}
