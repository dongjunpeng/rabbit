package com.buterfleoge.rabbit.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.buterfleoge.rabbit.RabbitWebContext;
import com.buterfleoge.rabbit.WebConfig;
import com.buterfleoge.rabbit.view.PdfView;
import com.buterfleoge.whale.Constants.Status;
import com.buterfleoge.whale.biz.OrderBiz;
import com.buterfleoge.whale.biz.OrderCancelBiz;
import com.buterfleoge.whale.biz.OrderCreateBiz;
import com.buterfleoge.whale.biz.OrderDiscountBiz;
import com.buterfleoge.whale.biz.OrderPayBiz;
import com.buterfleoge.whale.biz.OrderRefundBiz;
import com.buterfleoge.whale.dao.OrderInfoRepository;
import com.buterfleoge.whale.service.alipay.protocol.AlipayCreateReturnRequest;
import com.buterfleoge.whale.type.PayType;
import com.buterfleoge.whale.type.protocol.Error;
import com.buterfleoge.whale.type.protocol.Request;
import com.buterfleoge.whale.type.protocol.Response;
import com.buterfleoge.whale.type.protocol.order.CreateOrderRequest;
import com.buterfleoge.whale.type.protocol.order.GetBriefOrdersRequest;
import com.buterfleoge.whale.type.protocol.order.GetBriefOrdersResponse;
import com.buterfleoge.whale.type.protocol.order.GetDiscountRequest;
import com.buterfleoge.whale.type.protocol.order.GetDiscountResponse;
import com.buterfleoge.whale.type.protocol.order.GetOrderHistoryResponse;
import com.buterfleoge.whale.type.protocol.order.GetOrderResponse;
import com.buterfleoge.whale.type.protocol.order.GetRefundTypeResponse;
import com.buterfleoge.whale.type.protocol.order.NewOrderRequest;
import com.buterfleoge.whale.type.protocol.order.NewOrderResponse;
import com.buterfleoge.whale.type.protocol.order.OrderPayResultRequest;
import com.buterfleoge.whale.type.protocol.order.OrderPayResultResponse;
import com.buterfleoge.whale.type.protocol.order.OrderRequest;
import com.buterfleoge.whale.type.protocol.order.PayOrderByAlipayResponse;
import com.buterfleoge.whale.type.protocol.order.PayOrderRequest;
import com.buterfleoge.whale.type.protocol.order.PreviewContractRequest;
import com.buterfleoge.whale.type.protocol.order.RefundOrderRequest;

/**
 *
 * 订单相关处理
 *
 * @author Brent24
 *
 */
@Controller
@RequestMapping("/order")
public class OrderController extends RabbitController {

    private static final Logger LOG = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderBiz orderBiz;

    @Autowired
    private OrderCreateBiz orderCreateBiz;

    @Autowired
    private OrderCancelBiz orderCancelBiz;

    @Autowired
    private OrderPayBiz orderPayBiz;

    @Autowired
    private OrderRefundBiz orderRefundBiz;

    @Autowired
    private OrderDiscountBiz orderDiscountBiz;

    @Autowired
    private OrderInfoRepository orderInfoRepository;

    @ResponseBody
    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public Response newOrder(@Valid NewOrderRequest request) throws Exception {
        NewOrderResponse response = new NewOrderResponse();
        orderCreateBiz.newOrder(requireAccountid(), request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/order", method = RequestMethod.POST)
    public Response createOrder(@RequestBody @Valid CreateOrderRequest request) throws Exception {
        Response response = new Response();
        orderCreateBiz.createOrder(requireAccountid(), request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/order", method = RequestMethod.GET)
    public Response getOrder(OrderRequest request) throws Exception {
        GetOrderResponse response = new GetOrderResponse();
        orderBiz.getOrder(requireAccountid(), request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/brief", method = RequestMethod.GET)
    public Response getBriefOrder(GetBriefOrdersRequest request) throws Exception {
        GetBriefOrdersResponse response = new GetBriefOrdersResponse();
        orderBiz.getBriefOrders(requireAccountid(), request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/order", method = RequestMethod.DELETE)
    public Response cancelOrder(OrderRequest request) throws Exception {
        Response response = new Response();
        orderCancelBiz.cancelOrder(requireAccountid(), request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/history", method = RequestMethod.GET)
    public Response getOrderHistory(OrderRequest request) throws Exception {
        GetOrderHistoryResponse response = new GetOrderHistoryResponse();
        orderBiz.getOrderHistory(requireAccountid(), request, response);
        return response;
    }

    @RequestMapping(value = "/contract", method = RequestMethod.GET, produces = "application/pdf")
    public ModelAndView getOrderContract(OrderRequest request) throws Exception {
        Response response = new Response();
        String pdfPath = orderCreateBiz.getContract(requireAccountid(), request, response);
        ModelAndView modelAndView = new ModelAndView("pdfView");
        modelAndView.addObject(PdfView.PATH_KEY, pdfPath);
        return modelAndView;
    }

    @RequestMapping(value = "/contract/preview", method = RequestMethod.GET, produces = "application/pdf")
    public ModelAndView previewOrderContract(PreviewContractRequest request) throws Exception {
        Response response = new Response();
        String pdfPath = orderCreateBiz.previewContract(requireAccountid(), request, response);
        ModelAndView modelAndView = new ModelAndView("pdfView");
        modelAndView.addObject(PdfView.PATH_KEY, pdfPath);
        return modelAndView;
    }

    @RequestMapping(value = "/pay", method = RequestMethod.GET)
    public ModelAndView payOrder(PayOrderRequest request, HttpServletRequest httpServletRequest, HttpServletResponse httpResponse)
            throws Exception {
        PayOrderByAlipayResponse response = new PayOrderByAlipayResponse();
        request.setIp(RabbitWebContext.getRealIp());
        orderPayBiz.payOrder(requireAccountid(), request, response);

        ModelAndView modelAndView;
        if (request.getPayType() == PayType.ALIPAY.value) {
            modelAndView = new ModelAndView("alipay_form", response.getAlipayFrom());
        } else {
            modelAndView = new ModelAndView("wx_pay_jsapi", response.getWxJsapiModel());
        }
        return modelAndView;
    }

    @RequestMapping(value = "/alipay/return", method = RequestMethod.GET)
    public ModelAndView alipayReturn(AlipayCreateReturnRequest request, HttpServletRequest httpRequest) throws Exception {
        ModelAndView modelAndView;
        Response response = new Response();
        try {
            orderPayBiz.handleAlipayReturn(requireAccountid(), httpRequest.getParameterMap(), request, response);
            if (response.hasError()) {
                modelAndView = new ModelAndView("alipay_create_direct_pay_failed");
            } else {
                modelAndView = new ModelAndView("alipay_create_direct_pay_success");
            }
        } catch (Exception e) {
            LOG.error("handle alipay return failed, reqid: " + request.getReqid(), e);
            response.setStatus(Status.SYSTEM_ERROR);
            response.addError(new Error(e.getMessage()));
            modelAndView = new ModelAndView("alipay_create_direct_pay_failed");
        }
        modelAndView.addObject("orderid", request.getOut_trade_no());
        if (response.hasError()) {
            modelAndView.addObject("errMsg", response.getErrors().get(0).getMessage());
        }
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(value = "/payresult", method = RequestMethod.GET)
    public Response getWxpayResult(OrderPayResultRequest request) throws Exception {
        OrderPayResultResponse response = new OrderPayResultResponse();
        try {
            orderPayBiz.getOrderPayResult(requireAccountid(), request, response);
        } catch (Exception e) {
            LOG.error("get order pay result failed", e);
            response.setStatus(Status.SYSTEM_ERROR);
        }
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/refundtype", method = RequestMethod.GET)
    public Response getRefundType(OrderRequest request) throws Exception {
        GetRefundTypeResponse response = new GetRefundTypeResponse();
        orderRefundBiz.getRefundType(requireAccountid(), request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/refund", method = RequestMethod.POST)
    public Response refundOrder(RefundOrderRequest request) throws Exception {
        Response response = new Response();
        orderRefundBiz.refundOrder(requireAccountid(), request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/discount", method = RequestMethod.GET)
    public Response getDiscount(GetDiscountRequest request) throws Exception {
        GetDiscountResponse response = new GetDiscountResponse();
        orderDiscountBiz.getDiscount(requireAccountid(), request, response);
        return response;
    }

    @RequestMapping(value = "/{orderid}", method = RequestMethod.GET)
    public String getOrderPage(@PathVariable Long orderid, Request request, HttpServletRequest httpRequest)
            throws Exception {
        Long accountid = requireAccountid();
        try {
            if (orderInfoRepository.countByOrderidAndAccountid(orderid, accountid) == 1) {
                return isWeixinUserAgent(httpRequest) ? "worder" : "order";
            }
        } catch (Exception e) {
            LOG.error("find orderid failed, travelid: " + orderid + ", reqid: " + request.getReqid(), e);
        }
        return WebConfig.getNotfoundPage(httpRequest);
    }

}
