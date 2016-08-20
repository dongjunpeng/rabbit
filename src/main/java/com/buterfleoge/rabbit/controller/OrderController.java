package com.buterfleoge.rabbit.controller;

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

import com.buterfleoge.rabbit.WebConfig;
import com.buterfleoge.whale.biz.order.CreateOrderBiz;
import com.buterfleoge.whale.biz.order.OrderBiz;
import com.buterfleoge.whale.dao.OrderInfoRepository;
import com.buterfleoge.whale.type.entity.OrderInfo;
import com.buterfleoge.whale.type.protocol.Request;
import com.buterfleoge.whale.type.protocol.Response;
import com.buterfleoge.whale.type.protocol.order.CancelOrderRequest;
import com.buterfleoge.whale.type.protocol.order.CreateOrderRequest;
import com.buterfleoge.whale.type.protocol.order.CreateOrderResponse;
import com.buterfleoge.whale.type.protocol.order.GetBriefOrdersRequest;
import com.buterfleoge.whale.type.protocol.order.GetBriefOrdersResponse;
import com.buterfleoge.whale.type.protocol.order.GetDiscountRequest;
import com.buterfleoge.whale.type.protocol.order.GetDiscountResponse;
import com.buterfleoge.whale.type.protocol.order.GetOrderRequest;
import com.buterfleoge.whale.type.protocol.order.GetOrderResponse;
import com.buterfleoge.whale.type.protocol.order.NewOrderRequest;
import com.buterfleoge.whale.type.protocol.order.NewOrderResponse;
import com.buterfleoge.whale.type.protocol.order.PayOrderByAlipayResponse;
import com.buterfleoge.whale.type.protocol.order.PayOrderRequest;
import com.buterfleoge.whale.type.protocol.order.RefoundRequest;
import com.buterfleoge.whale.type.protocol.order.RefoundResponse;
import com.buterfleoge.whale.type.protocol.order.ValidateCodeRequest;
import com.buterfleoge.whale.type.protocol.order.ValidateCodeResponse;

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
    private CreateOrderBiz createOrderBiz;

    @Autowired
    private OrderInfoRepository orderInfoRepository;

    @ResponseBody
    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public NewOrderResponse newOrder(@Valid NewOrderRequest request) throws Exception {
        NewOrderResponse response = new NewOrderResponse();
        createOrderBiz.newOrder(requireAccountid(), request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/order", method = RequestMethod.POST)
    public Response createOrder(@RequestBody @Valid CreateOrderRequest request) throws Exception {
        CreateOrderResponse response = new CreateOrderResponse();
        createOrderBiz.createOrder(requireAccountid(), request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/order", method = RequestMethod.GET)
    public GetOrderResponse getOrder(GetOrderRequest request) throws Exception {
        GetOrderResponse response = new GetOrderResponse();
        orderBiz.getOrder(requireAccountid(), request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/order", method = RequestMethod.DELETE)
    public Response cancelOrder(CancelOrderRequest request) throws Exception {
        Response response = new Response();
        orderBiz.cancelOrder(requireAccountid(), request, response);
        return response;
    }

    @RequestMapping(value = "/pay", method = RequestMethod.GET)
    public void payOrder(PayOrderRequest request, HttpServletResponse httpResponse) throws Exception {
        PayOrderByAlipayResponse response = new PayOrderByAlipayResponse();
        try {
            orderBiz.payOrder(requireAccountid(), request, response);
            httpResponse.setHeader("Content-Type", "text/html;charset=UTF-8");
            httpResponse.getWriter().write(response.getAlipayFrom());
        } catch (Exception e) {
            LOG.error("pay order failed, reqid: " + request.getReqid(), e);
            httpResponse.sendRedirect(WebConfig.REDIRECT_FAILED);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/brief", method = RequestMethod.GET)
    public GetBriefOrdersResponse getRoute(GetBriefOrdersRequest request) throws Exception {
        GetBriefOrdersResponse response = new GetBriefOrdersResponse();
        orderBiz.getBriefOrders(requireAccountid(), request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/refound", method = RequestMethod.GET)
    public RefoundResponse getRefoundInfo(RefoundRequest request) throws Exception {
        RefoundResponse response = new RefoundResponse();
        orderBiz.getRefoundInfo(requireAccountid(), request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/discount", method = RequestMethod.GET)
    public GetDiscountResponse getDiscount(GetDiscountRequest request) throws Exception {
        GetDiscountResponse response = new GetDiscountResponse();
        orderBiz.getDiscount(requireAccountid(), request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/discountcode", method = RequestMethod.GET)
    public ValidateCodeResponse validateDiscountCode(ValidateCodeRequest request) throws Exception {
        ValidateCodeResponse response = new ValidateCodeResponse();
        orderBiz.validateDiscountCode(requireAccountid(), request, response);
        return response;
    }

    @RequestMapping(value = "/{orderid}", method = RequestMethod.GET)
    public String getOrderPage(@PathVariable Long orderid, Request request) throws Exception {
        Long accountid = requireAccountid();
        OrderInfo orderInfo = orderInfoRepository.findByOrderidAndAccountid(orderid, accountid);
        return orderInfo == null ? "redirect:/notfound" : "order";
    }

}
