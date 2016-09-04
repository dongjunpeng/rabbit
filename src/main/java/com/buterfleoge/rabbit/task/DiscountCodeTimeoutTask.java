package com.buterfleoge.rabbit.task;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.buterfleoge.whale.dao.DiscountCodeRepository;
import com.buterfleoge.whale.type.DiscountCodeStatus;
import com.buterfleoge.whale.type.entity.DiscountCode;

/**
 * @author Brent24
 *
 */
@Component
public class DiscountCodeTimeoutTask {

    private static final Set<Integer> CODECHECK = new HashSet<Integer>();

    static {
        // 优惠码 创建和验证但未使用的
        CODECHECK.add(DiscountCodeStatus.CREATED.value);
        CODECHECK.add(DiscountCodeStatus.VERIFIED.value);
    }

    @Autowired
    private DiscountCodeRepository discountCodeRepository;

    // 优惠码过期,每天00:01执行
    @Transactional(rollbackFor = Exception.class)
    @Scheduled(cron = "1 0 0 * * ? ")
    public void changeDiscountCodeStatus() {
        List<DiscountCode> codeList = discountCodeRepository.findByStatusIn(CODECHECK);
        for (DiscountCode discountCode : codeList) {
            if (discountCode.getEndTime().getTime() < System.currentTimeMillis()) {
                discountCode.setStatus(DiscountCodeStatus.TIMEOUT.value);
            }
        }
        discountCodeRepository.save(codeList);
    }

}
