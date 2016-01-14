package com.buterfleoge.rabbit.service;

import com.buterfleoge.whale.type.protocol.account.RegisterRequest;

/**
 * 
 *
 * @author xiezhenzong
 *
 */
public interface RegisterService {

    void registerByEmail(RegisterRequest request) throws Exception;


}
