package com.buterfleoge.rabbit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.buterfleoge.whale.dao.TravellerRepository;

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

}
