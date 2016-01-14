package com.buterfleoge.rabbit.service.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.buterfleoge.rabbit.service.RegisterService;
import com.buterfleoge.whale.biz.AccountBiz;
import com.buterfleoge.whale.type.AccountType;
import com.buterfleoge.whale.type.protocol.account.RegisterRequest;

/**
 * 各种方式组册
 *
 * @author xiezhenzong
 *
 */
@Service("registerService")
public class RegisterServiceImpl implements RegisterService {

    private static final Logger LOG = Logger.getLogger(RegisterServiceImpl.class);

    @Autowired
    private AccountBiz accountBiz;

    @Override
    public void registerByEmail(RegisterRequest request) throws Exception {
        String email = request.getEmail();
        String password = request.getPassword();
        try {
            if (accountBiz.isEmailExist(email)) {
                //
            }
        } catch (IllegalArgumentException e) {

        } catch (Exception e) {
            LOG.error("Check email exist failed", e);
        }

        try {
            accountBiz.createAccount(email, password, AccountType.user);
        } catch (IllegalArgumentException e) {

        } catch (Exception e) {

        }
    }

}
