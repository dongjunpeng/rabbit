package com.buterfleoge.rabbit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.buterfleoge.whale.biz.order.CodeGenerator;
import com.buterfleoge.whale.biz.order.OrderBiz;
import com.buterfleoge.whale.type.protocol.Response;
import com.buterfleoge.whale.type.protocol.order.CancelOrderRequest;
import com.buterfleoge.whale.type.protocol.order.CreateOrderRequest;
import com.buterfleoge.whale.type.protocol.order.GetBriefRequest;
import com.buterfleoge.whale.type.protocol.order.GetBriefResponse;
import com.buterfleoge.whale.type.protocol.order.GetDiscountRequest;
import com.buterfleoge.whale.type.protocol.order.GetDiscountResponse;
import com.buterfleoge.whale.type.protocol.order.GetOrdersRequest;
import com.buterfleoge.whale.type.protocol.order.GetOrdersResponse;
import com.buterfleoge.whale.type.protocol.order.TestRequest;
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
public class OrderController {

    @Autowired
    private OrderBiz orderBiz;

    @Autowired
    private CodeGenerator codeGenerator;

    @ResponseBody
    @RequestMapping(value = "/orders", method = RequestMethod.GET)
    public GetOrdersResponse getOrders(GetOrdersRequest request) throws Exception {
        GetOrdersResponse response = new GetOrdersResponse();
        orderBiz.getOrders(request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/order", method = RequestMethod.POST)
    public Response createOrder(@ModelAttribute("request") CreateOrderRequest request) throws Exception {
        Response response = new Response();
        orderBiz.createOrder(request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/order", method = RequestMethod.DELETE)
    public Response cancelOrder(CancelOrderRequest request) throws Exception {
        Response response = new Response();
        orderBiz.cancelOrder(request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/brief", method = RequestMethod.GET)
    public GetBriefResponse getRoute(GetBriefRequest request) throws Exception {
        GetBriefResponse response = new GetBriefResponse();
        orderBiz.getBriefOrders(request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/discount", method = RequestMethod.GET)
    public GetDiscountResponse getDiscount(GetDiscountRequest request) throws Exception {
        GetDiscountResponse response = new GetDiscountResponse();
        orderBiz.getDiscount(request, response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/discountcode", method = RequestMethod.POST)
    public ValidateCodeResponse validateDiscountCode(ValidateCodeRequest request) throws Exception {
        ValidateCodeResponse response = new ValidateCodeResponse();
        orderBiz.validateDiscountCode(request, response);
        return response;
    }

    // 下面都是测试用的
    @ResponseBody
    @RequestMapping(value = "/test/code", method = RequestMethod.POST)
    public Response validateDiscountCode(TestRequest request) throws Exception {
        Response response = new Response();
        codeGenerator.generate(request.getCount(), request.getValue(), request.getStartTime(), request.getEndTime());
        return response;
    }

    @RequestMapping(value = "/**", method = RequestMethod.GET)
    public String getOrderPage() throws Exception {
        return "order";
    }

}
