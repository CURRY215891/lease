package com.ls.lease.web.admin.schedule;

import com.ls.lease.model.entity.LeaseAgreement;
import com.ls.lease.model.enums.LeaseStatus;
import com.ls.lease.web.admin.service.LeaseAgreementService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ScheduledTasks {

    @Autowired
    private LeaseAgreementService service;

    @Scheduled(cron = "0 0 0 * * *")
    public void checkLeaseStatus() {
        LambdaUpdateWrapper<LeaseAgreement> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.le(LeaseAgreement::getLeaseEndDate, new Date());
        updateWrapper.in(LeaseAgreement::getStatus, LeaseStatus.SIGNED ,LeaseStatus.WITHDRAWING);
        updateWrapper.set(LeaseAgreement::getStatus, LeaseStatus.EXPIRED);
        service.update(updateWrapper);
    }
}
