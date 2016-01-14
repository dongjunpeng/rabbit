package com.buterfleoge.rabbit.controller;


import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.buterfleoge.whale.StatusObject;
import com.buterfleoge.whale.dao.InformationRepository;
import com.buterfleoge.whale.eo.Information;


@RestController
public class InformationController {

	@Autowired
	private InformationRepository informationRepository;

	//返回用户信息
	@RequestMapping(value = "/information/{userid}", method = RequestMethod.GET)
	public Information getInformation(@PathVariable long userid, HttpServletResponse response) {
		response.setStatus(200);
		return informationRepository.findByUserid(userid);
	}
	
	//更新用户信息
	@RequestMapping(value = "/information/{userid}", method = RequestMethod.POST)
	public StatusObject updateInformation(@PathVariable long userid, @ModelAttribute Information informationRequest,
			HttpServletResponse response) throws CloneNotSupportedException {
		Information information=informationRequest.clone();
		information.setUserid(userid);
		informationRepository.save(information);
		response.setStatus(200);
		return new StatusObject("success","information updated");
	}

}
