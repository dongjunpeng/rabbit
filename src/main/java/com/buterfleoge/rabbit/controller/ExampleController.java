package com.buterfleoge.rabbit.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * example controller
 * 
 * @author xiezhenzong
 *
 */
@RestController
@RequestMapping("/example")
public class ExampleController {

    @RequestMapping(method = RequestMethod.GET)
    public String getAllUser() {
        return "hello";
    }

}
