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

    private String watchPath;

    private MyConf conf;

    private CountDownLatch latch;

    /**
     * 设置等待
     * @param init 等待放行数
     */
    public void setLatch(int init) {
        this.latch = new CountDownLatch(init);
    }

    /**
     * 设置watch路径
     * @param watchPath watch路径
     */
    public void setWatchPath(String watchPath) {
        this.watchPath = watchPath;
    }

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
        zk.getData(watchPath, this, this, "NodeChanged");
    }

    @Override
    public void process(WatchedEvent event) {
        System.out.println(event.toString());
        Event.EventType type = event.getType();
        switch (type) {
            case None:
            case NodeChildrenChanged:
                break;
            case NodeCreated:
                System.out.println("watch: create new Node...");
                zk.getData(watchPath, this, this, "NodeCreated");
                break;
            case NodeDeleted:
                System.out.println("watch: delete Node...");
                // 节点被删除时清空配置信息，等待重新初始化
                conf.setConnStr("");
                latch = new CountDownLatch(1);
                break;
            case NodeDataChanged:
                System.out.println("watch: Node changed...");
                zk.getData(watchPath, this, this, "NodeChanged");
                break;
        }
    }

    /**
     * 获取数据
     */
    public void await() {

        // 阻塞获取配置信息
        try {
            zk.exists(watchPath, this, this, "initExists");
            System.out.println("wait for configuration...");
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
