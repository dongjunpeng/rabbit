package com.buterfleoge.rabbit.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.buterfleoge.whale.dao.DiscountCodeRepository;
import com.buterfleoge.whale.dao.OrderDiscountRepository;
import com.buterfleoge.whale.dao.OrderInfoRepository;
import com.buterfleoge.whale.dao.TravelGroupRepository;
import com.buterfleoge.whale.type.DiscountCodeStatus;
import com.buterfleoge.whale.type.DiscountType;
import com.buterfleoge.whale.type.GroupStatus;
import com.buterfleoge.whale.type.OrderStatus;
import com.buterfleoge.whale.type.OrderStatusCategory;
import com.buterfleoge.whale.type.entity.DiscountCode;
import com.buterfleoge.whale.type.entity.OrderDiscount;
import com.buterfleoge.whale.type.entity.OrderInfo;
import com.buterfleoge.whale.type.entity.TravelGroup;

/**
 * @author Brent24
 *
 */
@Component
public class OrderTimeoutTask {

    private static final Logger LOG = LoggerFactory.getLogger(OrderTimeoutTask.class);

    @Autowired
    private TravelGroupRepository travelGroupRepository;

    @Autowired
    private OrderInfoRepository orderInfoRepository;

    @Autowired
    private OrderDiscountRepository orderDiscountRepository;

    @Autowired
    private DiscountCodeRepository discountCodeRepository;

    // 订单状态改变每分钟检查数据库
    @Transactional(rollbackFor = Exception.class)
    @Scheduled(fixedRate = 1000 * 60)
    public void changeOrderStatus() {
        List<OrderInfo> orderList = getTimeoutOrder();
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }

        List<TravelGroup> travelGroups = new ArrayList<TravelGroup>(orderList.size());
        List<DiscountCode> discountCodes = new ArrayList<DiscountCode>(orderList.size());
        for (OrderInfo orderInfo : orderList) {
            orderInfo.setStatus(OrderStatus.TIMEOUT.value);

            TravelGroup group = travelGroupRepository.findOne(orderInfo.getGroupid());
            group.setStatus(GroupStatus.OPEN.value);
            group.setActualCount(group.getActualCount() - orderInfo.getCount());
            travelGroups.add(group);

            OrderDiscount orderDiscount = orderDiscountRepository.findByOrderidAndType(orderInfo.getOrderid(), DiscountType.COUPON.value);
            if (orderDiscount != null) {
                DiscountCode discountCode = discountCodeRepository.findByDiscountCode(orderDiscount.getDiscountCode());
                discountCode.setStatus(DiscountCodeStatus.VERIFIED.value);
                discountCodes.add(discountCode);
            }
        }
        // FIXME: 这里要是链表太长。。。
        orderInfoRepository.save(orderList);
        travelGroupRepository.save(travelGroups);
        if (discountCodes.size() > 0) {
            discountCodeRepository.save(discountCodes);
        }
    }

    private List<OrderInfo> getTimeoutOrder() {
        Date currentMinusTwoHour = DateUtils.addHours(new Date(), -2);
        try {
            return orderInfoRepository.findByStatusInAndAddTimeLessThan(OrderStatusCategory.NO_ALLOW_NEW.getOrderStatuses(),
                    currentMinusTwoHour);
        } catch (Exception e) {
            LOG.error("get timeout order failed", e);
            return Collections.emptyList();
        }
    }
}