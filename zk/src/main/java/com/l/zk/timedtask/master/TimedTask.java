package com.l.zk.timedtask.master;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author sin97.cn
 * @date 2019/7/24 10:59
 **/
public class TimedTask {
    private static final String CONNECT = "94.191.56.73:2181,94.191.56.73:2182,94.191.56.73:2183";
    private static final String PATH = "/timedTask/lock";
    private String machineName;

    public TimedTask(String machineName) {
        this.machineName = machineName;
    }

    /**
     * 连接zk
     */
    public void go()  {
        CountDownLatch latch = new CountDownLatch(1);
        ZooKeeper zooKeeper = null;
        try {
            zooKeeper = new ZooKeeper(CONNECT, 5000, watchedEvent -> {
                latch.countDown();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(machineName + "成功连接zk");

        //尝试成为master节点
        toBeMaster(zooKeeper, machineName);
    }

    private void toBeMaster(ZooKeeper zooKeeper, String machineName) {
        // 1.尝试去创建 临时无序节点
        // 2.如果创建成功，执行定时任务。如果创建失败，启动watcher
        // 3.如果收到临时节点被删除的通知，重新执行 步骤1
        zooKeeper.create(PATH, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, (code, path, ctx, name) -> {
            if (code == KeeperException.Code.OK.intValue()) {
                System.out.println(machineName + "<<<<<<" + "创建节点成功，成为master");
                try {
                    TimeUnit.SECONDS.sleep(5);
                    zooKeeper.delete(PATH, -1);
                    System.out.println(machineName + "<<<<<<" + "宕机");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (KeeperException e) {
                    e.printStackTrace();
                }

            } else if (code == KeeperException.Code.NODEEXISTS.intValue()) {
                //节点已存在，监听
                System.out.println(machineName + "<<<<<<" + "等待中----------");
                try {
                    zooKeeper.exists(PATH, watchedEvent -> {
                        if (watchedEvent.getType() == Watcher.Event.EventType.NodeDeleted) {
                            toBeMaster(zooKeeper, machineName);
                        }
                    });
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                //连接断开，会话过期等
                System.out.println("toBeMaster 其他状态" + code);
            }
        }, "ctx");


    }


}
