package com.buterfleoge.rabbit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
=======
	// 增加旅客信息
	@RequestMapping(path = "/traveller", method = RequestMethod.PUT)
	public StatusObject addTraveller(@ModelAttribute User userRequest, @ModelAttribute Traveller travellerRequest,
			HttpServletResponse response) throws CloneNotSupportedException {
		Traveller traveller = travellerRequest.clone();
		traveller.setUserid(userRequest.getUserid());
		travellerRepository.save(traveller);
		response.setStatus(201);
		return new StatusObject("success", "traveller created");
	}

	// 列出所有旅客信息
	@RequestMapping(path = "/traveller/{userid}", method = RequestMethod.GET)
	public Iterable<Traveller> getTravellerByUserid(@PathVariable("userid") long userid, HttpServletResponse response) {
		Iterable<Traveller> iterable = travellerRepository.findByUserid(userid);
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
	public StatusObject updateTraveller(@PathVariable("travellerId") long travellerId,
			@ModelAttribute Traveller travellerRequest, HttpServletResponse response)
					throws CloneNotSupportedException {
		Traveller traveller = travellerRequest.clone();
		traveller.setTravellerId(travellerId);
		travellerRepository.save(traveller);
		response.setStatus(200);
		return new StatusObject("success", "traveller updated");
	}

	// 删除旅客信息
	@RequestMapping(path = "/traveller/{travellerId}", method = RequestMethod.DELETE)
	public StatusObject deleteTraveller(@PathVariable("travellerId") long travellerId,HttpServletResponse response) {
		travellerRepository.delete(travellerId);
		response.setStatus(200);
		return new StatusObject("success","traveller deleted");
	}

}
