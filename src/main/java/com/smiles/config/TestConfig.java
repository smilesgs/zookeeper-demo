package com.smiles.config;

import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class TestConfig {

    private ZooKeeper zk;

    private MyConf conf = new MyConf();

    private WatchCallback watchCallback = new WatchCallback();

    @Before
    public void conn() {
        ZKConf zkConf = new ZKConf();
        zkConf.setAddress("192.168.2.63:2181,192.168.2.64:2181,192.168.2.65:2181,192.168.2.66:2181/smilesTest");
        zkConf.setSessionTime(1000);
        ZKUtils.setConf(zkConf);

        DefaultWatch defaultWatch = new DefaultWatch();
        ZKUtils.setDefaultWatch(defaultWatch);

        zk = ZKUtils.getZK();
    }

    @After
    public void close() {
        ZKUtils.closeZK();
    }

    @Test
    public void testConfig() {
        watchCallback.setConf(conf);
        watchCallback.setLatch(1);
        watchCallback.setZk(zk);
        watchCallback.setWatchPath("/AppConf");

        try {
            // 阻塞等待获取配置信息
            watchCallback.await();

            while(true) {

                // 当配置信息被清理时进行等待获取
                if (conf.getConnStr() == null || "".equals(conf.getConnStr().trim())) {
                    System.out.println("配置信息丢了。。。");
                    watchCallback.await();
                }

                System.out.println("connString: " + conf.getConnStr());

                TimeUnit.SECONDS.sleep(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
