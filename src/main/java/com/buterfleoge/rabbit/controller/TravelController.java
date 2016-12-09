/**
 *
 */
package com.buterfleoge.rabbit.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.buterfleoge.rabbit.WebConfig;
import com.buterfleoge.rabbit.view.PdfView;
import com.buterfleoge.whale.biz.TravelBiz;
import com.buterfleoge.whale.dao.TravelRouteRepository;
import com.buterfleoge.whale.type.protocol.Request;
import com.buterfleoge.whale.type.protocol.Response;
import com.buterfleoge.whale.type.protocol.travel.GetGroupRequest;
import com.buterfleoge.whale.type.protocol.travel.GetGroupResponse;
import com.buterfleoge.whale.type.protocol.travel.GetRouteRequest;
import com.buterfleoge.whale.type.protocol.travel.GetRouteResponse;
import com.buterfleoge.whale.type.protocol.travel.RouteRequest;

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

    @Value("${route.travelNoticePath}")
    private String travelNoticePath;

    @Value("${route.travelPreparePath}")
    private String travelPreparePath;

    @Value("${group.assemblePath}")
    private String assemblePath;

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

    @RequestMapping(value = "/travel_notice", method = RequestMethod.GET, produces = "application/pdf")
    public ModelAndView getTravelNotice(@Valid RouteRequest request) throws Exception {
        Long routeid = request.getRouteid();
        String pdfPath = travelNoticePath + "travel_notice_" + routeid + ".pdf";
        ModelAndView modelAndView = new ModelAndView("pdfView");
        modelAndView.addObject(PdfView.PATH_KEY, pdfPath);
        return modelAndView;
    }

    @RequestMapping(value = "/travel_prepare", method = RequestMethod.GET, produces = "application/pdf")
    public ModelAndView getTravelPrepare(@Valid RouteRequest request) throws Exception {
        Long routeid = request.getRouteid();
        String pdfPath = travelPreparePath + "travel_prepare_" + routeid + ".pdf";
        ModelAndView modelAndView = new ModelAndView("pdfView");
        modelAndView.addObject(PdfView.PATH_KEY, pdfPath);
        return modelAndView;
    }

    @RequestMapping(value = "/group_assemble", method = RequestMethod.GET, produces = "application/pdf")
    public ModelAndView getTravelPrepare(@Valid GetGroupRequest request) throws Exception {
        Long groupid = request.getGroupid();
        String pdfPath = assemblePath + "group_assemble_" + groupid + ".pdf";
        ModelAndView modelAndView = new ModelAndView("pdfView");
        modelAndView.addObject(PdfView.PATH_KEY, pdfPath);
        return modelAndView;
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
