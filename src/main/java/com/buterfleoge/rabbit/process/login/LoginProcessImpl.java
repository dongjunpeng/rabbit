package com.buterfleoge.rabbit.process.login;

import java.math.BigDecimal;
import java.util.Date;

import javax.annotation.Resource;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.buterfleoge.rabbit.WebConfig;
import com.buterfleoge.rabbit.process.LoginProcess;
import com.buterfleoge.whale.Utils;
import com.buterfleoge.whale.dao.AccountBindingRepository;
import com.buterfleoge.whale.dao.AccountInfoRepository;
import com.buterfleoge.whale.dao.ActivityRepository;
import com.buterfleoge.whale.dao.CouponRepository;
import com.buterfleoge.whale.dao.WxIdMappingRepository;
import com.buterfleoge.whale.exception.WeixinException;
import com.buterfleoge.whale.service.WeixinWebService;
import com.buterfleoge.whale.service.weixin.protocol.WxAccessTokenResponse;
import com.buterfleoge.whale.service.weixin.protocol.WxUserinfoResponse;
import com.buterfleoge.whale.type.AccountStatus;
import com.buterfleoge.whale.type.CouponType;
import com.buterfleoge.whale.type.IdType;
import com.buterfleoge.whale.type.entity.AccountBinding;
import com.buterfleoge.whale.type.entity.AccountInfo;
import com.buterfleoge.whale.type.entity.Activity;
import com.buterfleoge.whale.type.entity.Coupon;
import com.buterfleoge.whale.type.entity.WxIdMapping;

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
    @Resource(name = "weixinWebService")
    private WeixinWebService weixinWebService;

    @Autowired
    @Resource(name = "weixinCgibinService")
    private WeixinWebService weixinCgibinService;

    @Autowired
    private AccountInfoRepository accountInfoRepository;

    @Autowired
    private AccountBindingRepository accountBindingRepository;

    @Autowired
    private WxIdMappingRepository wxIdMappingRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Override
    public AccountInfo weixinWebLogin(String code) throws Exception {
        WxAccessTokenResponse accessToken = weixinWebService.getAccessToken(code);
        if (accessToken == null || accessToken.getErrcode() != null) {
            throw new WeixinException("Get access token from weixin failed");
        }
        AccountBinding weixinBinding = getWeixinBinding(accessToken.getUnionid());
        AccountInfo info = null;
        if (weixinBinding != null) {
            info = getAccountInfo(weixinBinding.getAccountid());
        } else {
            WxUserinfoResponse userinfoResponse = weixinWebService.getUserinfo(accessToken.getAccess_token(), accessToken.getOpenid());
            if (userinfoResponse == null || userinfoResponse.getErrcode() != null) {
                throw new WeixinException("Get user info from weixin failed");
            }
            info = createAccountInfo(userinfoResponse);
        }
        addAccessTokenToCache(info.getAccountid(), accessToken, WebConfig.getAccessTokenKey(info.getAccountid()));
        return info;
    }

    @Override
    public AccountInfo weixinWapBaseLogin(String code) throws WeixinException {
        WxAccessTokenResponse accessToken = weixinCgibinService.getAccessToken(code);
        if (accessToken == null || accessToken.getErrcode() != null) {
            throw new WeixinException("Get access token from weixin failed");
        }
        WxIdMapping wxIdMapping = wxIdMappingRepository.findByOpenid(accessToken.getOpenid());
        AccountInfo info = null;
        if (wxIdMapping != null) {
            info = getAccountInfo(wxIdMapping.getAccountid());
            addAccessTokenToCache(info.getAccountid(), accessToken, WebConfig.getWapAccessTokenKey(info.getAccountid()));
        } // 如果为null，则表明还未授权，需要用户进行手动授权
        return info;
    }

    @Override
    public AccountInfo weixinWapUserInfoLogin(String code) throws Exception {
        WxAccessTokenResponse accessToken = weixinCgibinService.getAccessToken(code);
        if (accessToken == null || accessToken.getErrcode() != null) {
            throw new WeixinException("Get access token from weixin failed");
        }
        AccountBinding weixinBinding = getWeixinBinding(accessToken.getUnionid());
        AccountInfo info = null;
        if (weixinBinding != null) {
            info = getAccountInfo(weixinBinding.getAccountid());
        } else { // 生成账号
            WxUserinfoResponse userinfoResponse = weixinCgibinService.getUserinfo(accessToken.getAccess_token(), accessToken.getOpenid());
            if (userinfoResponse == null || userinfoResponse.getErrcode() != null) {
                throw new WeixinException("Get user info from weixin failed");
            }
            info = createAccountInfoAndRecordWxIdMapping(userinfoResponse);
        }
        addAccessTokenToCache(info.getAccountid(), accessToken, WebConfig.getWapAccessTokenKey(info.getAccountid()));
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
    private AccountInfo createAccountInfo(WxUserinfoResponse userinfoResponse) throws Exception {
        AccountInfo accountInfo = insertAccountInfo(userinfoResponse);
        insertAccountSetting(accountInfo, userinfoResponse.getUnionid());
        sendNewCoupon(accountInfo);
        return accountInfo;
    }

    @Transactional(rollbackFor = Exception.class)
    private AccountInfo createAccountInfoAndRecordWxIdMapping(WxUserinfoResponse userinfoResponse) throws Exception {
        AccountInfo accountInfo = insertAccountInfo(userinfoResponse);
        insertAccountSetting(accountInfo, userinfoResponse.getUnionid());
        insertWxIdMapping(accountInfo, userinfoResponse);
        sendNewCoupon(accountInfo);
        return accountInfo;
    }

    private AccountInfo insertAccountInfo(WxUserinfoResponse userinfoResponse) throws WeixinException {
        AccountInfo info = new AccountInfo();
        info.setStatus(AccountStatus.WAIT_COMPLETE_INFO.value);
        info.setIdType(IdType.IDENTIFICATION.value);
        info.setNickname(Utils.filterOffUtf8Mb4(userinfoResponse.getNickname()));
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

    private void insertWxIdMapping(AccountInfo accountInfo, WxUserinfoResponse userinfoResponse) throws WeixinException {
        WxIdMapping wxIdMapping = new WxIdMapping();
        wxIdMapping.setAccountid(accountInfo.getAccountid());
        wxIdMapping.setOpenid(userinfoResponse.getOpenid());
        wxIdMapping.setUnionid(userinfoResponse.getUnionid());
        wxIdMapping.setAddTime(new Date());
        try {
            wxIdMappingRepository.save(wxIdMapping);
        } catch (Exception e) {
            throw new WeixinException("insert wxidmapping failed, info: " + accountInfo + ", wxidmapping: " + wxIdMapping, e);
        }
    }

    private void sendNewCoupon(AccountInfo accountInfo) throws Exception {
        Activity activity = activityRepository.findOne(Activity.ACTIVITY_NEW);
        if (activity != null && activity.isOpen()) {
            BigDecimal value = activity.getValue();
            Coupon coupon = Coupon.createCoupon(accountInfo.getAccountid(), "新人优惠", "新人登录即获" + Utils.formatPrice(value) + "优惠券",
                    CouponType.NEW, value);
            coupon.setEndTime(DateUtils.addMonths(coupon.getStartTime(), 6));
            try {
                couponRepository.save(coupon);
            } catch (Exception e) {
                throw new Exception("insert coupon failed, coupon: " + coupon);
            }
        }
    }

    private void addAccessTokenToCache(Long accountid, WxAccessTokenResponse accessToken, String cacheKey) {
        try {
            operations.set(cacheKey, accessToken);
        } catch (Exception e) {
            LOG.error("add access token to cache failed, accessToken: " + accessToken, e);
        }
    }

}
