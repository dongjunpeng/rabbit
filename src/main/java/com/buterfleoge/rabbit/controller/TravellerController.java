package com.buterfleoge.rabbit.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.buterfleoge.whale.dao.TravellerRepository;
import com.buterfleoge.whale.type.entity.AccountInfo;
import com.buterfleoge.whale.type.entity.TravellerInfo;
import com.buterfleoge.whale.type.protocol.Response;

@RestController
public class TravellerController {
    @Autowired
    private TravellerRepository travellerRepository;

    // @RequestMapping(path = "addTraveller", method = RequestMethod.POST)
    // public StatusObject addTraveller(@ModelAttribute User userRequest, @ModelAttribute Traveller
    // travellerRequest,HttpServletResponse response)
    // throws CloneNotSupportedException {
    // Traveller traveller = travellerRequest.clone();
    // traveller.setUserId(userRequest.getUserId());
    // travellerRepository.save(traveller);
    // response.setStatus(201);
    // return new StatusObject("success", "traveller created");
    // }

    // 增加旅客信息
    @RequestMapping(path = "/traveller", method = RequestMethod.PUT)
    public Response<String> addTraveller(@ModelAttribute AccountInfo userRequest,
            @ModelAttribute TravellerInfo travellerInfo, HttpServletResponse response)
                    throws CloneNotSupportedException {

        // traveller.setUserid(userRequest.getUserid());
        travellerRepository.save(travellerInfo);
        response.setStatus(201);
        return new Response<String>();
    }

    // 列出所有旅客信息
    @RequestMapping(path = "/traveller/{userid}", method = RequestMethod.GET)
    public Iterable<TravellerInfo> getTravellerByUserid(@PathVariable("userid") long userid,
            HttpServletResponse response) {
        Iterable<TravellerInfo> iterable = travellerRepository.findByAccountid(userid);
        if (iterable.iterator().hasNext()) {
            response.setStatus(200);
            return iterable;
        } else {
            response.setStatus(400);
            return null;
        }
    }

    // 修改旅客信息
    @RequestMapping(path = "/traveller/{travellerId}", method = RequestMethod.POST)
    public Response<String> updateTraveller(@PathVariable("travellerId") long travellerId,
            @ModelAttribute TravellerInfo travellerInfo, HttpServletResponse response)
                    throws CloneNotSupportedException {
        travellerInfo.setTravellerid(travellerId);
        travellerRepository.save(travellerInfo);
        response.setStatus(200);
        return new Response<String>();
    }

    // 删除旅客信息
    @RequestMapping(path = "/traveller/{travellerId}", method = RequestMethod.DELETE)
    public Response<String> deleteTraveller(@PathVariable("travellerId") long travellerId,
            HttpServletResponse response) {
        travellerRepository.delete(travellerId);
        response.setStatus(200);
        return new Response<String>();
    }

}
