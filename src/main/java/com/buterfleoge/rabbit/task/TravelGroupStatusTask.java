package com.buterfleoge.rabbit.task;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.buterfleoge.whale.dao.TravelGroupRepository;
import com.buterfleoge.whale.type.GroupStatus;
import com.buterfleoge.whale.type.entity.TravelGroup;

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
}
