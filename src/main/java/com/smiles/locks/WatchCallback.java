package com.smiles.locks;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class WatchCallback implements Watcher, AsyncCallback.StringCallback, AsyncCallback.Children2Callback, AsyncCallback.StatCallback {

    /** zk实例 */
    private ZooKeeper zk;

    /** 当前线程名 */
    private String threadName;

    /** 线程对应路径名 */
    private String pathName;

    /** 加锁的共用路径 */
    private String LockPath = "/lock_";

    private CountDownLatch latch = new CountDownLatch(1);

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public void process(WatchedEvent event) {
        Event.EventType type = event.getType();
        switch (type) {
            case None:
                break;
            case NodeCreated:
                break;
            case NodeDeleted:
                // 此处会通知当前节点的下一个节点去获取锁
                this.zk.getChildren("/", false, this, "getChildren");
                break;
            case NodeDataChanged:
                break;
            case NodeChildrenChanged:
                break;
        }
    }

    public void tryLock() {
        try {
            // 临时sequence + watch
            zk.create(LockPath, threadName.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL, this,"create");
            System.out.println(threadName + " tryLock...");
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void unLock() {
        try {
            this.zk.delete(pathName, -1);
            System.out.println(threadName + " work over...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processResult(int rc, String path, Object ctx, String name) {
        if (name == null || "".equals(name)) {
            return;
        }

        pathName = name;
        System.out.println(threadName + "create node:" + name);
        // 此处只是获取子节点，然后根据子节点的顺序在设置对前一节点的watch，此处不需要进行watch
        this.zk.getChildren("/", false, this, "getChildren");
    }

    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {
        if (children == null || children.isEmpty()) {
            System.out.println("list is empty, " + ctx.toString());
            return;
        }

        Collections.sort(children);
        int index = children.indexOf(pathName.substring(1));
        // 序列最小的节点获取到锁，若是其他节点则对前一节点设置watch
        if (index == 0) {
            try {
                // 序列最小的节点获取到锁，将根节点的内容设置为当前获取锁的名称，后续可以通过此来实现锁的可重入性
                this.zk.setData("/", pathName.getBytes(), -1);
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            latch.countDown();
            System.out.println(threadName + " get Lock,work begin...");
        } else {
            // 查看前一个节点是否存在，并设置对前一个节点的watch
            this.zk.exists("/" + children.get(index - 1), this, this, "exits");
        }
    }

    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        // 前一个节点为空则重新判断当前节点是否是当前目录中最小的节点，若是则加锁
        if (stat == null) {
            this.zk.getChildren("/", false, this, "getChildren");
        }
    }
}
