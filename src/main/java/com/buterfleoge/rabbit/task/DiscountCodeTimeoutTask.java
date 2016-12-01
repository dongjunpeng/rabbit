package com.buterfleoge.rabbit.task;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.buterfleoge.whale.dao.CouponRepository;
import com.buterfleoge.whale.type.CouponStatus;
import com.buterfleoge.whale.type.entity.Coupon;

/**
 * @author Brent24
 *
 */
@Component
public class DiscountCodeTimeoutTask {

    @Autowired
    private CouponRepository discountCodeRepository;

    // 优惠码过期,每天00:01执行
    @Transactional(rollbackFor = Exception.class)
    @Scheduled(cron = "1 0 0 * * ? ")
    public void changeDiscountCodeStatus() {
        List<Coupon> codeList = discountCodeRepository.findByStatus(CouponStatus.CREATED.value);
        for (Coupon discountCode : codeList) {
            if (discountCode.getEndTime().getTime() < System.currentTimeMillis()) {
                discountCode.setStatus(CouponStatus.TIMEOUT.value);
                discountCode.setModTime(new Date());
            }
        }
        discountCodeRepository.save(codeList);
    }

}
