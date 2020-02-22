package com.smiles.config;

import org.apache.zookeeper.ZooKeeper;

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
     * 计数器
     */
    private static CountDownLatch latch = new CountDownLatch(1);

    /**
     * zk配置
     */
    private static ZKConf conf;

    /**
     * 默认watch
     */
    private static DefaultWatch watch;

    /**
     * 设置zk配置信息
     *
     * @param conf zk配置信息
     */
    public static void setConf(ZKConf conf) {
        ZKUtils.conf = conf;
    }

    /**
     * 设置默认的watch
     *
     * @param watch 默认的watch
     */
    public static void setDefaultWatch(DefaultWatch watch) {
        watch.setLatch(latch);
        ZKUtils.watch = watch;

    }

    /**
     * 获取zk实例
     * @return zk实例
     */
    public static ZooKeeper getZK(){
        try {
            zk = new ZooKeeper(conf.getAddress(), conf.getSessionTime(), watch);
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return zk;
    }

    /**
     * 关闭zk实例
     */
    public static void closeZK(){
        if(zk != null){
            try {
                zk.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
