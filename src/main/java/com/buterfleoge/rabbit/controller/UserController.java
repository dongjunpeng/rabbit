package com.buterfleoge.rabbit.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.buterfleoge.whale.StatusObject;
import com.buterfleoge.whale.dao.InformationRepository;
import com.buterfleoge.whale.dao.UserRepository;
import com.buterfleoge.whale.eo.Information;
import com.buterfleoge.whale.eo.User;

/**
 * @author dongjunpeng
 *
 */

@RestController
public class UserController {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private InformationRepository informationRepository;

	// 邮箱是否存在
	@RequestMapping(path = "/email/{email}", method = RequestMethod.GET)
	public StatusObject emailExists(@PathVariable String email, HttpServletResponse response) {
		if (userRepository.countByEmail(email) > 0) {
			response.setStatus(400);
			return new StatusObject("fail", "email exists");
		} else {
			response.setStatus(200);
			return new StatusObject("success", "email dosen't exist");
		}
	}

	// 通过邮箱注册
	@RequestMapping(value = "/emailRegister", method = RequestMethod.POST)
	public StatusObject emailRegister(@ModelAttribute User userRequest, HttpServletResponse response) {
		User user = userRepository.save(userRequest);
		Information information = new Information();
		information.setUserId(user.getUserId());
		informationRepository.save(information);
		response.setStatus(200);
		return new StatusObject("success", "email registered");
	}

	// 邮箱登录
	@RequestMapping(value = "/emailLogin", method = RequestMethod.POST)
	public StatusObject emailLogin(@ModelAttribute User userRequest, HttpServletResponse response) {
		User user = userRepository.findByEmail(userRequest.getEmail());
		if (user == null) {
			response.setStatus(400);
			return new StatusObject("fail", "email dosen't exist");
		}
		if (userRequest.getPassword().equals(user.getPassword())) {
			response.setStatus(200);
			return new StatusObject("success", "successfully log in");
		} else {
			response.setStatus(400);
			return new StatusObject("fail", "email or password is not correct");
		}
	}

	// 修改密码
	@RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
	public StatusObject updatePassword(@ModelAttribute User userRequest,
			@RequestParam("newPassword") String newPassword, HttpServletResponse response) {
		User user = userRepository.findByUserId(userRequest.getUserId());
		if (!userRequest.getPassword().equals(user.getPassword())) {
			response.setStatus(400);
			return new StatusObject("fail", "password is not correct");
		} else {
			user.setPassword(newPassword);
			userRepository.save(user);
			response.setStatus(200);
			return new StatusObject("success", "password updated");
		}
	}

}
