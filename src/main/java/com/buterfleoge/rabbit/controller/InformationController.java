package com.buterfleoge.rabbit.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.buterfleoge.whale.dao.InformationRepository;
import com.buterfleoge.whale.type.entity.AccountSetting;
import com.buterfleoge.whale.type.protocol.Response;

@RestController
public class InformationController {

    @Autowired
    private InformationRepository informationRepository;

    // 返回用户信息
    @RequestMapping(value = "/information/{userid}", method = RequestMethod.GET)
    public AccountSetting getInformation(@PathVariable long userid, HttpServletResponse response) {
        response.setStatus(200);
        return informationRepository.findByAccountid(userid);
    }

    // 更新用户信息
    @RequestMapping(value = "/information/{accountid}", method = RequestMethod.POST)
    public Response<String> updateInformation(@PathVariable long accountid, @ModelAttribute AccountSetting accountSetting,
            HttpServletResponse response) throws CloneNotSupportedException {
    	accountSetting.setAccountid(accountid);;
        informationRepository.save(accountSetting);
        response.setStatus(200);
        return new Response<String>();
    }

}
