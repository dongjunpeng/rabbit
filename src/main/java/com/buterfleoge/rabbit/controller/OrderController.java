package com.buterfleoge.rabbit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.buterfleoge.whale.biz.order.OrderBiz;
import com.buterfleoge.whale.type.protocol.order.GetCurrentOrderRequest;
import com.buterfleoge.whale.type.protocol.order.GetCurrentOrderResponse;

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

    @ResponseBody
    @RequestMapping(value = "/current", method = RequestMethod.GET)
    public GetCurrentOrderResponse getRoute(GetCurrentOrderRequest request) throws Exception {
        GetCurrentOrderResponse response = new GetCurrentOrderResponse();
        orderBiz.getCurrentOrder(request, response);
        return response;
    }

}
