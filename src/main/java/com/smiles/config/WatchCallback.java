package com.smiles.config;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

/**
 * 实现watch 和 获取数据的相关回调接口
 */
public class WatchCallback implements Watcher, AsyncCallback.StatCallback, AsyncCallback.DataCallback {

    private ZooKeeper zk;

    private MyConf conf;

    private CountDownLatch latch = new CountDownLatch(1);

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    public void setConf(MyConf conf) {
        this.conf = conf;
    }

    @Override
    public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
        // 获取配置信息，成功获取时则放行
        if (data == null) {
            return;
        }
        conf.setConnStr(new String(data));
        latch.countDown();
    }

    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        if (stat == null) {
            return;
        }
        // 节点不为空时则获取对应配置信息
        zk.getData("/smileConfig", this, this, "smile");
    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                zk.getData("/smileConfig", this, this, "smile");
                break;
            case NodeDeleted:
                // 节点被删除时清空配置信息，等待重新初始化
                conf.setConnStr("");
                latch = new CountDownLatch(1);
                break;
            case NodeDataChanged:
                zk.getData("/smileConfig", this, this, "smile");
                break;
            case NodeChildrenChanged:
                break;
        }
    }

    /**
     * 获取数据
     */
    public void await() {
        zk.exists("/smileConfig", this, this, "smile");

        // 阻塞获取配置信息
        try {
            System.out.println("wait for configuration...");
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
