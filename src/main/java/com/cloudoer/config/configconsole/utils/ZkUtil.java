package com.cloudoer.config.configconsole.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;

import java.util.List;

/**
 * @author liuxiaokun
 * @version 0.0.1
 * @since 2018/9/18
 */
@Slf4j
public class ZkUtil {

    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    CuratorFramework client;


    CuratorFramework init() {
        client =
                CuratorFrameworkFactory.builder()
                        .connectString("127.0.0.1:2181")
                        .sessionTimeoutMs(5000)
                        .connectionTimeoutMs(5000)
                        .retryPolicy(retryPolicy)
                        .build();

        client.start();
        return client;
    }


    public static void main(String[] args) throws Exception {

        new ZkUtil().init();

//        Stat stat = new Stat();
//        byte[] bytes = client.getData().storingStatIn(stat).forPath("/config/config-zookeeper/user/foo");
//        log.info("result:{}", new String(bytes));

        //client.create().creatingParentContainersIfNeeded().forPath("/test-node1/tn1/tn2/tn3", "test-data4".getBytes());

//        List<String> strings = client.getChildren().forPath("/config");
//
//        strings.forEach(log::info);

//        client.delete().deletingChildrenIfNeeded().forPath("/zookeeper");
    }
}
