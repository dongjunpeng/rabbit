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
import com.buterfleoge.whale.type.entity.Information;

@RestController
public class InformationController {

	@Autowired
	private InformationRepository informationRepository;

	//返回用户信息
	@RequestMapping(value = "/information/{userId}", method = RequestMethod.GET)
	public Information getInformation(@PathVariable long userId, HttpServletResponse response) {
		response.setStatus(200);
		return informationRepository.findByUserId(userId);
	}
	
	//更新用户信息
	@RequestMapping(value = "/updateInformation", method = RequestMethod.POST)
	public StatusObject updateInformation(@ModelAttribute Information informationRequest, HttpServletResponse response) {
		Information information=informationRepository.findByUserId(informationRequest.getUserId());
		information.setNickname(informationRequest.getNickname());
		information.setGender(informationRequest.getGender());
		information.setBirthday(informationRequest.getBirthday());
		informationRepository.save(information);
		response.setStatus(200);
		return new StatusObject("success","information updated");
	}

}
