package com.yy.shardingjdbcdemo.sharding;

import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

public class MyShardingString implements PreciseShardingAlgorithm<String> {
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {
        String id = shardingValue.getValue();

        // orderId的hashcode值 对 节点个数 取模
        int mode = id.hashCode() % availableTargetNames.size();

        // 分片节点数组
        String[] strings = availableTargetNames.toArray(new String[0]);
        mode = Math.abs(mode);

        System.out.println(strings[0] + "---------" + strings[1]);
        System.out.println("mode=" + mode);
        return strings[mode];
    }
}
