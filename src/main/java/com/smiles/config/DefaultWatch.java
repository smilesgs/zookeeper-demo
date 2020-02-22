package com.smiles.config;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.concurrent.CountDownLatch;

public class DefaultWatch implements Watcher {

    private CountDownLatch latch;

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void process(WatchedEvent event) {
        Event.KeeperState state = event.getState();
        switch (state) {
            case Disconnected:
                System.out.println("Disconnected...wait...");
                this.latch = new CountDownLatch(1);
                break;
            case SyncConnected:
                System.out.println("Connected...ok...");
                this.latch.countDown();
                break;
        }
    }
}
