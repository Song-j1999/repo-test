package com.yy.xademo.service;

import com.yy.xademo.db131.dao.XA131Mapper;
import com.yy.xademo.db131.model.XA131;
import com.yy.xademo.db132.dao.XA132Mapper;
import com.yy.xademo.db132.model.XA132;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class XAService {

    @Resource
    private XA131Mapper xa131Mapper;
    @Resource
    private XA132Mapper xa132Mapper;

    @Transactional(transactionManager = "xaTransaction")
    public void testXA() {
        XA131 xa131 = new XA131();
        xa131.setId(1);
        xa131.setName("xa_131");
        xa131Mapper.insert(xa131);

        XA132 xa132 = new XA132();
        xa132.setId(1);
        xa132.setName("xa_132");
        xa132Mapper.insert(xa132);

    }
}
