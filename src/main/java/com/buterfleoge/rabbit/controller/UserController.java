package com.buterfleoge.rabbit.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.buterfleoge.whale.dao.UserRepository;
import com.buterfleoge.whale.eo.StatusObject;
import com.buterfleoge.whale.eo.User;

/**
 * @author dongjunpeng
 *
 */
@RestController
public class UserController {

	@Autowired
	private UserRepository userRepository;

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

	@RequestMapping(value = "/emailRegister", method = RequestMethod.POST)
	public StatusObject emailRegister(@ModelAttribute("user") User user, HttpServletResponse response) {
		userRepository.save(user);
		response.setStatus(200);
		return new StatusObject("success", "email registered");
	}

}
