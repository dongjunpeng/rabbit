package com.buterfleoge.rabbit.process.impl;

import java.util.Date;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.buterfleoge.rabbit.WebConfig;
import com.buterfleoge.rabbit.process.LoginProcess;
import com.buterfleoge.whale.dao.AccountBindingRepository;
import com.buterfleoge.whale.dao.AccountInfoRepository;
import com.buterfleoge.whale.exception.WeixinException;
import com.buterfleoge.whale.service.WeixinWebService;
import com.buterfleoge.whale.service.weixin.protocol.WxAccessTokenResponse;
import com.buterfleoge.whale.service.weixin.protocol.WxUserinfoResponse;
import com.buterfleoge.whale.type.AccountStatus;
import com.buterfleoge.whale.type.IdType;
import com.buterfleoge.whale.type.entity.AccountBinding;
import com.buterfleoge.whale.type.entity.AccountInfo;

/**
 *
 * @author xiezhenzong
 *
 */
@Component("loginProcess")
public class LoginProcessImpl implements LoginProcess {

    private static final Logger LOG = LoggerFactory.getLogger(LoginProcessImpl.class);

    @Resource(name = "cacheTemplate")
    private ValueOperations<String, Object> operations;

    @Autowired
    private WeixinWebService weixinWebService;

    @Autowired
    private AccountInfoRepository accountInfoRepository;

    @Autowired
    private AccountBindingRepository accountBindingRepository;

    @Override
    public AccountInfo weixinWebLogin(String code) throws WeixinException {
        WxAccessTokenResponse accessToken = weixinWebService.getAccessToken(code);
        if (accessToken == null || accessToken.getErrcode() != null) {
            throw new WeixinException("Get access token from weixin failed");
        }
        WxUserinfoResponse userinfoResponse = weixinWebService.getUserinfo(accessToken.getAccess_token(),
                accessToken.getOpenid());
        if (userinfoResponse == null || userinfoResponse.getErrcode() != null) {
            throw new WeixinException("Get user info from weixin failed");
        }
        AccountBinding weixinBinding = getWeixinBinding(userinfoResponse.getUnionid());
        AccountInfo info = null;
        if (weixinBinding != null) {
            info = getAccountInfo(weixinBinding.getAccountid());
        } else {
            info = createAccountInfo(userinfoResponse);
        }
        addAccessTokenToCache(info.getAccountid(), accessToken);
        return info;
    }

    private AccountBinding getWeixinBinding(String wxid) throws WeixinException {
        try {
            return accountBindingRepository.findByWxid(wxid);
        } catch (Exception e) {
            throw new WeixinException("Get weixin binding from db failed", e);
        }
    }

    private AccountInfo getAccountInfo(Long accountid) throws WeixinException {
        try {
            return accountInfoRepository.findOne(accountid);
        } catch (Exception e) {
            throw new WeixinException("Get account info from db failed", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    private AccountInfo createAccountInfo(WxUserinfoResponse userinfoResponse)
            throws WeixinException {
        AccountInfo accountInfo = insertAccountInfo(userinfoResponse);
        insertAccountSetting(accountInfo, userinfoResponse.getUnionid());
        return accountInfo;
    }

    private AccountInfo insertAccountInfo(WxUserinfoResponse userinfoResponse) throws WeixinException {
        AccountInfo info = new AccountInfo();
        info.setStatus(AccountStatus.WAIT_COMPLETE_INFO.value);
        info.setIdType(IdType.IDENTIFICATION.value);
        info.setNickname(userinfoResponse.getNickname());
        info.setBirthday(new Date());
        info.setGender(userinfoResponse.getSex());
        info.setAvatarUrl(userinfoResponse.getHeadimgurl());
        info.setAddTime(new Date());
        info.setModTime(info.getAddTime());

        try {
            return accountInfoRepository.save(info);
        } catch (Exception e) {
            throw new WeixinException("Insert account info to db failed", e);
        }
    }

    private void insertAccountSetting(AccountInfo info, String wxid) throws WeixinException {
        AccountBinding binding = new AccountBinding();
        binding.setAccountid(info.getAccountid());
        binding.setWxid(wxid);
        binding.setModTime(info.getAddTime());

        try {
            accountBindingRepository.save(binding);
        } catch (Exception e) {
            throw new WeixinException("insert account setting failed, info: " + info + ", binding: " + binding, e);
        }
    }

    private void addAccessTokenToCache(Long accountid, WxAccessTokenResponse accessToken) {
        try {
            String cacheKey = WebConfig.getAccessTokenKey(accountid);
            operations.set(cacheKey, accessToken);
        } catch (Exception e) {
            LOG.error("add access token to cache failed, accessToken: " + accessToken, e);
        }
    }

}
