package com.smiles.config;

import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class TestConfig {

    private ZooKeeper zk;

    @Before
    public void conn() {
        try {
            zk = ZKUtils.getZK();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void close() {
        try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testConfig() {
        WatchCallback watch = new WatchCallback();
        MyConf conf = new MyConf();
        watch.setConf(conf);
        watch.setZk(zk);

        // 阻塞等待获取配置信息
        watch.await();

        while(true) {

            // 当配置信息被清理时进行等待获取
            if (conf.getConnStr() == null || "".equals(conf.getConnStr().trim())) {
                System.out.println("配置信息丢了。。。");
                watch.await();
            }

            System.out.println("connString: " + conf.getConnStr());
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
