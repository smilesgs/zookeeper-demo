package com.smiles;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class App 
{
    public static void main( String[] args ) throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);
        // watch：观察，回调,watch的注册只发生在读类型的调用，如：get，exists等。因为写方法是要产生事件的
        // watch分为两类
        // 第一类：new zk的时候传入的watch，这个watch是session级别的，跟path、node没有关系，
        // session上发生了任何事情都会回调这个watch,这个watch不是一次性的，只对client的连接状态做出反应
        // 第二类是节点上的watch，对这个节点的操作会执行的watch
        // 设置session的超时时间为3000ms
        final ZooKeeper zk = new ZooKeeper("192.168.2.63:2181,192.168.2.64:2181,192.168.2.65:2181,192.168.2.66:2181",
                3000, new Watcher() {
            // watch的回调方法
            @Override
            public void process(WatchedEvent watchedEvent) {
                Event.KeeperState state = watchedEvent.getState();
                Event.EventType type = watchedEvent.getType();
                String path = watchedEvent.getPath();
                System.out.println("new zk watch: " + watchedEvent.toString());

                switch (state) {
                    case Unknown:
                        break;
                    case Disconnected:
                        break;
                    case NoSyncConnected:
                        break;
                    case SyncConnected:
                        System.out.println("connected");
                        latch.countDown();
                        break;
                    case AuthFailed:
                        break;
                    case ConnectedReadOnly:
                        break;
                    case SaslAuthenticated:
                        break;
                    case Expired:
                        break;
                }

                switch (type) {
                    case None:
                        break;
                    case NodeCreated:
                        break;
                    case NodeDeleted:
                        break;
                    case NodeDataChanged:
                        break;
                    case NodeChildrenChanged:
                        break;
                }

            }
        });

        // 由于上面创建zk的时候是异步进行的，所以此处如果不等待直接往下执行，此时的状态是connecting
        latch.await();
        ZooKeeper.States state = zk.getState();
        switch (state) {
            case CONNECTING:
                System.out.println("connecting");
                break;
            case ASSOCIATING:
                break;
            case CONNECTED:
                System.out.println("connected");
                break;
            case CONNECTEDREADONLY:
                break;
            case CLOSED:
                break;
            case AUTH_FAILED:
                break;
            case NOT_CONNECTED:
                break;
        }

        // 创建节点方式两种，一种同步阻塞的，一种异步的
        // public String create(final String path, byte data[], List<ACL> acl,
        //        CreateMode createMode)
        // public void create(final String path, byte data[], List<ACL > acl,
        //        CreateMode createMode, AsyncCallback.StringCallback cb, Object ctx)
        String pathName = zk.create("/smile", "oldDate".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

        // 四种获取数据的方法，按watch分两类：一种是需传入watch的，一种是不传的[不传如果是需要watch则是使用的注册zk时的watch]，按是否同步分为同步和异步，
        // watch是一次性的
        // 元数据，即包含zxid等数据
        final Stat stat = new Stat();
        // 节点数据，当前这个方法是同步阻塞的，添加watch是在get方法上，因此节点必须存在才能添加watch
        byte[] node = zk.getData("/smile", new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("get Data watch: " + event.toString());
                // 由于watch是一次性的，只需要再使用完成之后继续注册就行
                try {
                    // 此处watch标识符为true代表使用default watch，即new zookeeper时的那个watch
//                    zk.getData("/smile", true, stat);
                    // 传入this则代表使用的是当前使用的watch
                    zk.getData("/smile", this, stat);
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, stat);

        System.out.println("/smile节点存储数据为：" + new String(node));

        // 修改数据，此时会触发上面注册的回调,由于watch是一次性的所以只有第一次修改打印了，第二次修改没有打印提示信息
        // 如果在watch中进行重复注册watch，则可以打印出第二次的修改
        Stat stat1 = zk.setData("/smile", "newData".getBytes(), stat.getVersion());
        // 由于循环注册的时候需要花费时间，如果修改先于注册可能就此次修改就执行不了
        Thread.sleep(1);
        Stat stat2 = zk.setData("/smile", "newData01".getBytes(), stat1.getVersion());

        // 异步获取数据的方式
        // 异步执行的好处：是方法内容的缔造者，而不是逻辑执行顺序的缔造者，逻辑执行顺序是有框架决定的，只需要把方法实现了放在那，
        // 未来事件发生的时候直接调用就行，这个时候整个计算机的逻辑是：什么事件发生了就立即处理那些事，尽量减少等待的空段
        System.out.println("=============async begin=============");
        zk.getData("/smile", false, new AsyncCallback.DataCallback(){

            @Override
            public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                System.out.println("=============async result=============");
                System.out.println("执行结果【0：执行成功】:" + rc);
                System.out.println("ctx:" + ctx);
                System.out.println("data:" + new String(data));
                System.out.println("path:" + path);
                System.out.println("=============async result=============");
            }
        }, "abc");

        System.out.println("=============async over=============");

//        Stat stat3 = zk.setData("/smile", "newData02".getBytes(), stat2.getVersion());
        String pathName2 = zk.create("/smile/sss", "Hello ".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        TimeUnit.MINUTES.sleep(99999999);
    }
}
