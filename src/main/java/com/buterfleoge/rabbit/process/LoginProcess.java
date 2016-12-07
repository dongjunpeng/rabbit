package com.buterfleoge.rabbit.process;

import com.buterfleoge.whale.type.entity.AccountInfo;

/**
 *
 * @author xiezhenzong
 *
 */
public interface LoginProcess {

    AccountInfo weixinWebLogin(String code) throws Exception;

    AccountInfo weixinWapBaseLogin(String code) throws Exception;

    AccountInfo weixinWapUserInfoLogin(String code) throws Exception;

}
