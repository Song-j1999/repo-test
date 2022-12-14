package com.yy.distributelock.controller;

import com.yy.distributelock.dao.DistributeLockMapper;
import com.yy.distributelock.model.DistributeLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Slf4j
public class DemoController {
    @Resource
    private DistributeLockMapper distributeLockMapper;

    @RequestMapping("dbLock")
    @Transactional(rollbackFor = Exception.class)
    public String dbLock() throws Exception {
        log.info("我进入了方法！");
        DistributeLock distributeLock = distributeLockMapper.selectDistributeLock("demo");
        if (distributeLock==null) throw new Exception("分布式锁找不到");
        log.info("我进入了锁！");
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "我已经执行完成！";
    }
}
