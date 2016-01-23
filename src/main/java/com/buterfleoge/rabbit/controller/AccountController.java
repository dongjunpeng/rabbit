package com.buterfleoge.rabbit.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.buterfleoge.rabbit.service.impl.RegisterServiceImpl;
import com.buterfleoge.whale.biz.AccountBiz;
import com.buterfleoge.whale.type.AccountType;
import com.buterfleoge.whale.type.protocol.Response;

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
	private AccountBiz accountBiz;
	private static final Logger LOG = Logger.getLogger(RegisterServiceImpl.class);

	@RequestMapping("/qq")
	public String qqVisit() {

		return null;
	}

	@RequestMapping("/wx")
	public String wxVisit() {

		return null;
	}

	// email是否存在
	@ResponseBody
	@RequestMapping("/email")
	public Response<String> emailExist(@RequestParam("email") String email) {

		Response<String> response = new Response<String>();

		try {
			// 存在返回fail，不存在返回ok
			if (accountBiz.isEmailExist(email)) {
				response.setStatus(Response.STATUS_FAIL);
				response.setShowMessage("email已注册!");
				return response;
			} else {
				response.setStatus(Response.STATUS_OK);
				return response;
			}
		} catch (IllegalArgumentException e) {
			// email是否合法
			response.setStatus(Response.STATUS_PARAM_ERROR);
			response.setShowMessage("email参数非法!");
			return response;
		} catch (Exception e) {
			LOG.error("Check email exist failed", e);
			response.setStatus(Response.STATUS_SYS_ERROR);
			response.setShowMessage("查询失败");
			return response;
		}

	}

	// email注册
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	@ResponseBody
	public Response<String> register(@RequestParam("email") String email, @RequestParam("password") String password) {

		Response<String> response = new Response<String>();

		try {
			accountBiz.createAccount(email, password, AccountType.COMMON);
			response.setStatus(Response.STATUS_OK);
			return response;
		} catch (IllegalArgumentException e) {
			response.setStatus(Response.STATUS_PARAM_ERROR);
			response.setShowMessage(e.getMessage());
			return response;
		} catch (Exception e) {
			LOG.error("register failed", e);
			response.setStatus(Response.STATUS_SYS_ERROR);
			response.setShowMessage("注册失败");
			return response;
		}
	}

	// email登陆
	@RequestMapping("/login")
	@ResponseBody
	public Response<String> login(@RequestParam("email") String email, @RequestParam("password") String password) {
		Response<String> response = new Response<String>();

		try {
			if (accountBiz.emailLogin(email, password)) {
				response.setStatus(Response.STATUS_OK);
				return response;
			} else {
				response.setStatus(Response.STATUS_FAIL);
				response.setShowMessage("EMAIL或密码错误");
				return response;
			}
		} catch (IllegalArgumentException e) {
			response.setStatus(Response.STATUS_PARAM_ERROR);
			response.setShowMessage(e.getMessage());
			return response;
		} catch (Exception e) {
			LOG.error("login failed", e);
			response.setStatus(Response.STATUS_SYS_ERROR);
			response.setShowMessage("登陆失败");
			return response;
		}

	}

}
