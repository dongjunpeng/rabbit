package com.buterfleoge.rabbit.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.buterfleoge.whale.service.WeixinCgibinService;
import com.buterfleoge.whale.service.weixin.protocol.WxCgibinAccessTokenResponse;

/**
 * @author xiezhenzong
 *
 */
@Component
public class WxTask {

    @Autowired
    private WeixinCgibinService weixinCgibinService;

    // 1000 * 7200 是两小时， 1000 * 7200为了保证在两小时内肯定被调度
    @Scheduled(fixedRate = 1000 * 7000)
    public void refreshAccessToken() {
        weixinCgibinService.getCgibinAccessToken();
    }

    @Scheduled(fixedRate = 1000 * 7000)
    public void refreshJsApiTicket() {
        WxCgibinAccessTokenResponse accessToken = weixinCgibinService.getCgibinAccessToken();
        if (accessToken != null) {
            weixinCgibinService.getTicket(accessToken.getAccess_token(), "jsapi");
        }
    }

}
