package com.buterfleoge.rabbit.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.buterfleoge.whale.biz.OrderPayBiz;
import com.buterfleoge.whale.service.alipay.protocol.AlipayCreateNotifyRequest;
import com.buterfleoge.whale.type.protocol.Response;

/**
 * @author xiezhenzong
 *
 */
@Controller
@RequestMapping("/ali")
public class AliController extends RabbitController {

    private static final Logger LOG = LoggerFactory.getLogger(AliController.class);

    @Autowired
    private OrderPayBiz payOrderBiz;

    @RequestMapping(value = "/alipay/notify", method = RequestMethod.POST)
    public void alipayNotify(AlipayCreateNotifyRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse)
            throws Exception {
        Response response = new Response();
        try {
            payOrderBiz.handleAlipayNotify(httpRequest.getParameterMap(), request, response);
            if (response.hasError()) {
                httpResponse.getWriter().write("failed");
            } else {
                httpResponse.getWriter().write("success");
            }
        } catch (Exception e) {
            LOG.error("handle alipay notify failed, reqid: " + request.getReqid(), e);
            httpResponse.getWriter().write("failed");
        }
    }

}
