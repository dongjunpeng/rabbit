package com.buterfleoge.rabbit.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.buterfleoge.whale.StatusObject;
import com.buterfleoge.whale.dao.TravellerRepository;
import com.buterfleoge.whale.dao.UserRepository;
import com.buterfleoge.whale.eo.Traveller;
import com.buterfleoge.whale.eo.User;

@RestController
public class TravellerController {
	@Autowired
	private TravellerRepository travellerRepository;

	@RequestMapping(path = "addTraveller", method = RequestMethod.POST)
	public StatusObject addTraveller(@ModelAttribute User userRequest, @ModelAttribute Traveller travellerRequest,HttpServletResponse response)
			throws CloneNotSupportedException {
		Traveller traveller = travellerRequest.clone();
		traveller.setUserId(userRequest.getUserId());
		travellerRepository.save(traveller);
		response.setStatus(201);
		return new StatusObject("success", "traveller created");
	}

}
