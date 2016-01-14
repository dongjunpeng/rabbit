package com.buterfleoge.rabbit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.buterfleoge.whale.dao.AccountInfoRepository;
import com.buterfleoge.whale.dao.InformationRepository;

/**
 * @author dongjunpeng
 *
 */

@RestController
public class UserController {

	@Autowired
	private AccountInfoRepository accountInfoRepository;
	@Autowired
	private InformationRepository informationRepository;

    // // 邮箱是否存在
    // @RequestMapping(path = "/email/{email}", method = RequestMethod.GET)
    // public StatusObject emailExists(@PathVariable String email, HttpServletResponse response) {
    // if (accountInfoRepository.countByEmail(email) > 0) {
    // response.setStatus(400);
    // return new StatusObject("fail", "email exists");
    // } else {
    // response.setStatus(200);
    // return new StatusObject("success", "email dosen't exist");
    // }
    // }
    //
    // // 通过邮箱注册
    // @RequestMapping(path = "/emailRegister", method = RequestMethod.POST)
    // public StatusObject emailRegister(@ModelAttribute User userRequest, HttpServletResponse response)
    // throws CloneNotSupportedException {
    // User user = accountInfoRepository.save(userRequest.clone());
    // // 生成用户的同时生成一个空的用户信息
    // Information information = new Information();
    // information.setUserId(user.getUserId());
    // informationRepository.save(information);
    // response.setStatus(200);
    // return new StatusObject("success", "email registered");
    // }
    //
    // // 邮箱登录
    // @RequestMapping(path = "/emailLogin", method = RequestMethod.POST)
    // public StatusObject emailLogin(@ModelAttribute User userRequest, HttpServletResponse response) {
    // User user = accountInfoRepository.findByEmail(userRequest.getEmail());
    // if (user == null) {
    // response.setStatus(400);
    // return new StatusObject("fail", "email dosen't exist");
    // }
    // if (userRequest.getPassword().equals(user.getPassword())) {
    // response.setStatus(200);
    // return new StatusObject("success", "successfully log in");
    // } else {
    // response.setStatus(400);
    // return new StatusObject("fail", "email or password is not correct");
    // }
    // }
    //
    // // 修改密码
    // @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
    // public StatusObject updatePassword(@ModelAttribute User userRequest,
    // @RequestParam("newPassword") String newPassword, HttpServletResponse response) {
    // User user = accountInfoRepository.findByUserId(userRequest.getUserId());
    // if (!userRequest.getPassword().equals(user.getPassword())) {
    // response.setStatus(400);
    // return new StatusObject("fail", "password is not correct");
    // } else {
    // user.setPassword(newPassword);
    // accountInfoRepository.save(user);
    // response.setStatus(200);
    // return new StatusObject("success", "password updated");
    // }
    // }

}
