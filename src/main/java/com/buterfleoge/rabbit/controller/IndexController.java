package com.buterfleoge.rabbit.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.buterfleoge.rabbit.view.PdfView;
import com.buterfleoge.whale.biz.travel.TravelBiz;
import com.buterfleoge.whale.type.protocol.Response;
import com.buterfleoge.whale.type.protocol.order.PreviewContractRequest;
import com.buterfleoge.whale.type.protocol.travel.GetRouteRequest;
import com.buterfleoge.whale.type.protocol.travel.GetRouteResponse;

/**
 *
 * @author xiezhenzong
 *
 */
@Controller
@RequestMapping("/index")
public class IndexController extends RabbitController {

    @Autowired
    private TravelBiz travelBiz;

    @ResponseBody
    @RequestMapping(value = "/hot", method = RequestMethod.GET)
    public Response getHotRoute(GetRouteRequest request, HttpServletRequest req) throws Exception {
        request.setFromWx(isWeixinUserAgent(req));
        GetRouteResponse response = new GetRouteResponse();
        travelBiz.getRoute(request, response);
        response.setRoutes(response.getRoutes().subList(0, 4));
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/wap/hot", method = RequestMethod.GET)
    public Response getWapHotRoute(GetRouteRequest request, HttpServletRequest req) throws Exception {
        request.setFromWx(isWeixinUserAgent(req));
        GetRouteResponse response = new GetRouteResponse();
        travelBiz.getRoute(request, response);
        response.setRoutes(response.getRoutes().subList(0, 2));
        return response;
    }

    @RequestMapping(value = "/hxy_secure_notice", method = RequestMethod.GET)
    public ModelAndView getHxySecureNotice(PreviewContractRequest request) throws Exception {
        ModelAndView modelAndView = new ModelAndView("pdfView");
        modelAndView.addObject(PdfView.PATH_KEY, "classpath:hxy_secure_notice.pdf");
        return modelAndView;
    }

    @RequestMapping(value = "/hxy_signup_notice", method = RequestMethod.GET)
    public ModelAndView getHxyXXXNotice(PreviewContractRequest request) throws Exception {
        ModelAndView modelAndView = new ModelAndView("pdfView");
        modelAndView.addObject(PdfView.PATH_KEY, "classpath:hxy_signup_notice.pdf");
        return modelAndView;
    }

}
