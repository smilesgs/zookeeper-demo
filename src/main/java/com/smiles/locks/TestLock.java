package com.smiles.locks;

import com.smiles.config.DefaultWatch;
import com.smiles.config.ZKConf;
import com.smiles.config.ZKUtils;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 模拟分布式锁的情况
 */
public class TestLock {

    ZooKeeper zk;

    @Before
    public void conn() {
        ZKConf conf = new ZKConf();
        conf.setAddress("192.168.2.63:2181,192.168.2.64:2181,192.168.2.65:2181,192.168.2.66:2181/lockTest");
        conf.setSessionTime(1000);
        ZKUtils.setConf(conf);

        DefaultWatch watch = new DefaultWatch();
        ZKUtils.setDefaultWatch(watch);

        zk = ZKUtils.getZK();
    }

    @After
    public void close() {
        ZKUtils.closeZK();
    }

    @Test
    public void testLock() {
        // 启动十个线程模拟分布式锁发生的情况
        for (int i = 0; i < 10; i++) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    WatchCallback watchCallback = new WatchCallback();
                    watchCallback.setZk(zk);
                    watchCallback.setThreadName(Thread.currentThread().getName());

                    // 加锁
                    watchCallback.tryLock();

                    // 业务逻辑
//                    try {
//                        TimeUnit.SECONDS.sleep(1);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }

                    // 释放锁
                    watchCallback.unLock();

                }
            }.start();
        }

        while (true) {
        }
    }

}
