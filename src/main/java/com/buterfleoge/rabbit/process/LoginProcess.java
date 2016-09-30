package com.buterfleoge.rabbit.process;

import com.buterfleoge.whale.exception.WeixinException;
import com.buterfleoge.whale.type.entity.AccountInfo;

/**
 *
 * @author xiezhenzong
 *
 */
public interface LoginProcess {

    AccountInfo weixinWebLogin(String code) throws WeixinException;

    AccountInfo weixinWapBaseLogin(String code) throws WeixinException;

    AccountInfo weixinWapUserInfoLogin(String code) throws WeixinException;

}
