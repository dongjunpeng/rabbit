package com.buterfleoge.rabbit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.buterfleoge.whale.dao.UserRepository;
import com.buterfleoge.whale.eo.User;

/**
 * example controller
 * 
 * @author xiezhenzong
 *
 */
@RestController
@RequestMapping("/example")
public class ExampleController {

    @Autowired
    private UserRepository userRepository;

    @RequestMapping(path = "/{userid}", method = RequestMethod.GET)
    public User getAllUser(@PathVariable Long userid) {
        return userRepository.findOne(userid);
    }

}
