package com.buterfleoge.rabbit.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.buterfleoge.whale.Utils;
import com.buterfleoge.whale.dao.AssemblyInfoRepository;
import com.buterfleoge.whale.dao.OrderInfoRepository;
import com.buterfleoge.whale.dao.OrderTravellersRepository;
import com.buterfleoge.whale.dao.TravelGroupRepository;
import com.buterfleoge.whale.dao.TravelRouteRepository;
import com.buterfleoge.whale.service.ShortMessageService;
import com.buterfleoge.whale.type.GroupStatus;
import com.buterfleoge.whale.type.OrderStatus;
import com.buterfleoge.whale.type.entity.AssemblyInfo;
import com.buterfleoge.whale.type.entity.OrderInfo;
import com.buterfleoge.whale.type.entity.OrderTraveller;
import com.buterfleoge.whale.type.entity.TravelGroup;
import com.buterfleoge.whale.type.entity.TravelRoute;

/**
 * @author Brent24
 *
 */
@Component
public class TravelGroupStatusTask {

    private static final Set<Integer> GROUPCHECK = new HashSet<Integer>();

    static {
        // 团状态 招募中和已满的
        GROUPCHECK.add(GroupStatus.OPEN.value);
        GROUPCHECK.add(GroupStatus.FULL.value);
        GROUPCHECK.add(GroupStatus.TRAVELLING.value);
    }

    @Autowired
    private TravelGroupRepository travelGroupRepository;

    @Autowired
    private TravelRouteRepository travelRouteRepository;

    @Autowired
    private OrderInfoRepository orderInfoRepository;

    @Autowired
    private OrderTravellersRepository orderTravellersRepository;

    @Autowired
    private AssemblyInfoRepository assemblyInfoRepository;

    @Autowired
    private ShortMessageService shortMessageService;

    // group状态改变,每天00:01执行
    @Transactional(rollbackFor = Exception.class)
    @Scheduled(cron = "1 0 0 * * ? ")
    public void changeTravelGroupStatus() {
        List<TravelGroup> groupList = travelGroupRepository.findByStatusIn(GROUPCHECK);
        for (TravelGroup travelGroup : groupList) {
            if (travelGroup.getStartDate().getTime() < System.currentTimeMillis()) {
                travelGroup.setStatus(GroupStatus.TRAVELLING.value);
            }
            if (travelGroup.getEndDate().getTime() < System.currentTimeMillis()) {
                travelGroup.setStatus(GroupStatus.FINISHED.value);
            }
        }
        travelGroupRepository.save(groupList);
    }

    // group状态改变,每天00:01执行
    @Transactional(rollbackFor = Exception.class)
    @Scheduled(cron = "0 10 0 * * ? ")
    public void sendAssemblyInfo() {
        Iterable<AssemblyInfo> assemblyInfo = assemblyInfoRepository.findAll();
        List<OrderInfo> toSave = new ArrayList<OrderInfo>();
        for (AssemblyInfo info : assemblyInfo) {
            if (!info.getReady()) {
                continue;
            }
            TravelGroup travelGroup = travelGroupRepository.findOne(info.getGroupid());
            if (travelGroup == null || travelGroup.getStartDate().getTime() > DateUtils.addWeeks(new Date(), 1).getTime()) {
                continue;
            }
            TravelRoute travelRoute = travelRouteRepository.findOne(travelGroup.getRouteid());
            if (travelRoute == null) {
                continue;
            }
            List<OrderInfo> orderInfos = orderInfoRepository.findByRouteidAndGroupidAndStatusIn(travelRoute.getRouteid(),
                    travelGroup.getGroupid(), Utils.asSet(OrderStatus.PAID.value));
            if (orderInfos.isEmpty()) {
                continue;
            }
            for (Iterator<OrderInfo> iterator = orderInfos.iterator(); iterator.hasNext();) {
                OrderInfo orderInfo = iterator.next();
                if (orderInfo.getSent()) {
                    continue;
                }
                List<OrderTraveller> orderTravellers = orderTravellersRepository.findByOrderid(orderInfo.getOrderid());
                if (shortMessageService.sendAssemblyInfo(travelRoute, travelGroup, info, orderTravellers)) {
                    orderInfo.setSent(Boolean.TRUE);
                    toSave.add(orderInfo);
                }
            }
        }
        if (toSave.size() > 0) {
            orderInfoRepository.save(toSave);
        }
    }
}
