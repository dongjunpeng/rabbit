package com.buterfleoge.rabbit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.buterfleoge.rabbit.service.RegisterService;
import com.buterfleoge.whale.type.protocol.account.RegisterRequest;

/**
 * 账户相关处理
 *
 * @author xiezhenzong
 *
 */
@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private RegisterService registerService;

    @RequestMapping("/login")
    public String login() {

        return null;
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(RegisterRequest request) {

        try {
            registerService.registerByEmail(request);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
