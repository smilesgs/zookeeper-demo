package com.smiles.config;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * zk工具类
 */
public class ZKUtils {

    /**
     * zk
     */
    private volatile static ZooKeeper zk;

    /**
     * 连接信息
     */
    private static final String connectionString = "192.168.2.63:2181,192.168.2.64:2181,192.168.2.65:2181,192.168.2.66:2181/smilestest";

    /**
     * 计数器
     */
    private static CountDownLatch latch = new CountDownLatch(1);

    /**
     * 默认watch
     */
    private static DefaultWatch watch = new DefaultWatch();

    public static ZooKeeper getZK() throws Exception {
        if (zk == null) {
            synchronized (ZKUtils.class){
                if (zk == null) {
                    watch.setLatch(latch);
                    zk = new ZooKeeper(connectionString, 3000, watch);
                    latch.await();
                }
            }
        }

        return zk;
    }
}
