package com.yy.rabbit.producer.task;

import java.util.List;

import com.yy.rabbit.producer.broker.RabbitBroker;
import com.yy.rabbit.producer.constant.BrokerMessageStatus;
import com.yy.rabbit.producer.entity.BrokerMessage;
import com.yy.rabbit.producer.service.MessageStoreService;
import com.yy.rabbit.task.annotation.ElasticJobConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;

import lombok.extern.slf4j.Slf4j;

@Component
@ElasticJobConfig(
        name = "com.bfxy.rabbit.producer.task.RetryMessageDataflowJob",
        cron = "0/10 * * * * ?",
        description = "可靠性投递消息补偿任务",
        overwrite = true,
        shardingTotalCount = 1
)
@Slf4j
public class RetryMessageDataflowJob implements DataflowJob<BrokerMessage> {

    @Autowired
    private MessageStoreService messageStoreService;

    @Autowired
    private RabbitBroker rabbitBroker;

    private static final int MAX_RETRY_COUNT = 3;

    /**
     * 抓取数据
     *
     * @param shardingContext
     * @return
     */
    @Override
    public List<BrokerMessage> fetchData(ShardingContext shardingContext) {
        List<BrokerMessage> list = messageStoreService.fetchTimeOutMessage4Retry(BrokerMessageStatus.SENDING);
        log.info("--------@@@@@ 抓取数据集合, 数量：	{} 	@@@@@@-----------", list.size());
        return list;
    }

    /**
     * 处理数据
     * @param shardingContext
     * @param dataList
     */
    @Override
    public void processData(ShardingContext shardingContext, List<BrokerMessage> dataList) {

        dataList.forEach(brokerMessage -> {

            String messageId = brokerMessage.getMessageId();
            if (brokerMessage.getTryCount() >= MAX_RETRY_COUNT) {
                this.messageStoreService.failure(messageId);
                log.warn(" -----消息设置为最终失败，消息ID: {} -------", messageId);
            } else {
                //	每次重发的时候要更新一下try count字段
                this.messageStoreService.updateTryCount(messageId);
                // 	重发消息
                this.rabbitBroker.reliantSend(brokerMessage.getMessage());
            }

        });
    }


}
