package com.buterfleoge.rabbit.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.buterfleoge.whale.Constants.SessionKey;
import com.buterfleoge.whale.biz.account.AccountBiz;
import com.buterfleoge.whale.dao.AccountSettingRepository;
import com.buterfleoge.whale.type.AccountType;
import com.buterfleoge.whale.type.protocol.Response;
import com.buterfleoge.whale.type.protocol.account.DeleteContactsRequest;
import com.buterfleoge.whale.type.protocol.account.EmailExistRequest;
import com.buterfleoge.whale.type.protocol.account.GetBasicInfoRequest;
import com.buterfleoge.whale.type.protocol.account.GetBasicInfoResponse;
import com.buterfleoge.whale.type.protocol.account.GetContactsRequest;
import com.buterfleoge.whale.type.protocol.account.GetContactsResponse;
import com.buterfleoge.whale.type.protocol.account.GetOrdersRequest;
import com.buterfleoge.whale.type.protocol.account.GetOrdersResponse;
import com.buterfleoge.whale.type.protocol.account.LoginRequest;
import com.buterfleoge.whale.type.protocol.account.PostBasicInfoRequest;
import com.buterfleoge.whale.type.protocol.account.PostContactsRequest;
import com.buterfleoge.whale.type.protocol.account.PostContactsResponse;
import com.buterfleoge.whale.type.protocol.account.PutContactsRequest;
import com.buterfleoge.whale.type.protocol.account.RegisterRequest;
import com.buterfleoge.whale.type.protocol.account.RegisterResponse;
import com.buterfleoge.whale.type.protocol.account.ValidateEmailRequest;

/**
 * 账户相关处理
 *
 * @author xiezhenzong
 *
 */
@Controller
@RequestMapping("/account")
public class AccountController {

	private static final Logger LOG = LoggerFactory.getLogger(AccountController.class);

	@Autowired
	private AccountBiz accountBiz;

	@Autowired
	private AccountSettingRepository accountSettingRepository;

	@Autowired
	private HttpServletRequest httpRequest;

	@ResponseBody
	@RequestMapping(value = "/email", method = RequestMethod.POST)
	public Response checkEmailExist(EmailExistRequest request) throws Exception {
		Response response = new Response();
		accountBiz.isEmailExist(request, response);
		return response;
	}

	@ResponseBody
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public RegisterResponse register(RegisterRequest request) throws Exception {

		RegisterResponse response = new RegisterResponse();
		request.setType(AccountType.USER);
		accountBiz.registerByEmail(request, response);
		if (response.hasError()) {
			return response;
		}
		response.getAccountInfo().setPassword(null);
		HttpSession session = httpRequest.getSession(true);
		session.setAttribute(SessionKey.ACCOUNT_BASIC_INF, response.getAccountInfo());
		return response;
	}

	// 测试用
	@ResponseBody
	@RequestMapping(value = "/basicinfo", method = RequestMethod.POST)
	public Response putBasicInfo() throws Exception {
		Response response = new Response();

		accountBiz.createBasicInfo();
		response.setStatus(0);
		return response;
	}

	@ResponseBody
	@RequestMapping(value = "/basicinfo", method = RequestMethod.GET)
	public GetBasicInfoResponse getBasicInfo(GetBasicInfoRequest request) throws Exception {
		GetBasicInfoResponse response = new GetBasicInfoResponse();
		accountBiz.getBasicInfo(request, response);
		return response;
	}

	@ResponseBody
	@RequestMapping(value = "/basicinfo", method = RequestMethod.PUT)
	public Response updateBasicInfo(PostBasicInfoRequest request) throws Exception {

		Response response = new Response();
		accountBiz.updateBasicInfo(request, response);
		return response;
	}

	@ResponseBody
	@RequestMapping(value = "/validateEmail", method = RequestMethod.GET)
	public Response validateEmail(ValidateEmailRequest request) throws Exception {
		Response response = new Response();
		accountBiz.validateEmail(request, response);
		return response;
	}

	@ResponseBody
	@RequestMapping(value = "/contacts", method = RequestMethod.GET)
	public GetContactsResponse getContacts(GetContactsRequest request) throws Exception {
		GetContactsResponse response = new GetContactsResponse();
		accountBiz.getContacts(request, response);
		return response;
	}

	@ResponseBody
	@RequestMapping(value = "/contacts", method = RequestMethod.POST)
	public Response postContacts(PostContactsRequest request) throws Exception {
		PostContactsResponse response = new PostContactsResponse();
		accountBiz.postContacts(request, response);
		return response;
	}

	@ResponseBody
	@RequestMapping(value = "/contacts", method = RequestMethod.PUT)
	public Response putContacts(PutContactsRequest request) throws Exception {
		Response response = new Response();
		accountBiz.putContacts(request, response);
		return response;
	}

	@ResponseBody
	@RequestMapping(value = "/contacts", method = RequestMethod.DELETE)
	public Response deleteContacts(DeleteContactsRequest request) throws Exception {
		Response response = new Response();
		accountBiz.deleteContacts(request, response);
		return response;
	}

	@ResponseBody
	@RequestMapping(value = "/orders", method = RequestMethod.GET)
	public GetOrdersResponse getOrders(GetOrdersRequest request) {

		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/orders", method = RequestMethod.POST)
	public GetOrdersResponse postOrders(GetOrdersRequest request) {

		return null;
	}

	// email登陆
	@ResponseBody
	@RequestMapping("/login")
	public Response login(LoginRequest request) {
		// Response<AccountInfo> response = new Response<AccountInfo>();
		// try {
		// request.getFirstDataItem().setType(AccountType.USER);
		// accountBiz.loginByEmail(request, response);
		// } catch (Exception e) {
		// LOG.error("Login failed", e);
		// response.setStatus(Status.SYSTEM_ERROR);
		// }
		// return response;
		return null;
	}

}
