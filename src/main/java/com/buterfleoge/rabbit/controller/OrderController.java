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
import com.buterfleoge.whale.type.protocol.account.object.AccountBasicInfo;
import com.buterfleoge.whale.type.protocol.order.CancelOrderRequest;
import com.buterfleoge.whale.type.protocol.order.CreateOrderRequest;
import com.buterfleoge.whale.type.protocol.order.GenerateCodeRequest;
import com.buterfleoge.whale.type.protocol.order.GetBriefRequest;
import com.buterfleoge.whale.type.protocol.order.GetBriefResponse;
import com.buterfleoge.whale.type.protocol.order.GetDiscountRequest;
import com.buterfleoge.whale.type.protocol.order.GetDiscountResponse;
import com.buterfleoge.whale.type.protocol.order.GetOrderDetailRequest;
import com.buterfleoge.whale.type.protocol.order.GetOrderDetailResponse;
import com.buterfleoge.whale.type.protocol.order.NewOrderRequest;
import com.buterfleoge.whale.type.protocol.order.NewOrderResponse;
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

    @Autowired
    private OrderBiz orderBiz;

    @Autowired
    private CodeGenerator codeGenerator;

    @ResponseBody
    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public NewOrderResponse newOrder(NewOrderRequest request) throws Exception {
        AccountBasicInfo accountBasicInfo = getAccountBasicInfo();
        // Long accountid = accountBasicInfo.getAccountInfo().getAccountid();
        Long accountid = request.getAccountid();
        NewOrderResponse response = new NewOrderResponse();
        orderBiz.newOrder(accountid, request, response);
        return response;
    }

    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public GetOrderDetailResponse getOrderDetail(GetOrderDetailRequest request) throws Exception {
        GetOrderDetailResponse response = new GetOrderDetailResponse();
        orderBiz.getOrderDetail(request, response);
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

    @ResponseBody
    @RequestMapping(value = "/refound", method = RequestMethod.GET)
    public RefoundResponse getRefoundInfo(RefoundRequest request) throws Exception {
        RefoundResponse response = new RefoundResponse();
        orderBiz.getRefoundInfo(request, response);
        return response;
    }

    @RequestMapping(value = "/**", method = RequestMethod.GET)
    public String getOrderPage() throws Exception {
        return "order";
    }

    // 下面都是测试用的
    @ResponseBody
    @RequestMapping(value = "/generate/code", method = RequestMethod.POST)
    public Response generateCode(GenerateCodeRequest request) throws Exception {
        Response response = new Response();
        codeGenerator.generate(request.getCount(), request.getValue(), request.getStartTime(), request.getEndTime());
        return response;
    }

}
