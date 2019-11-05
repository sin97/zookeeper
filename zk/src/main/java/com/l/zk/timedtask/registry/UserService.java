package com.l.zk.timedtask.registry;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author LJQ
 * @date 2019/7/25 10:15
 **/
public class UserService {
    private static final String USER_PATH = "/service/user";
    private static final String ORDER_PATH = "/service/order";
    private static final String CLUSTER = "94.191.56.73:2181,94.191.56.73:2182,94.191.56.73:2183";

    public void start(String ip) {
        CountDownLatch latch = new CountDownLatch(1);
        ZooKeeper zooKeeper = null;
        try {
            zooKeeper = new ZooKeeper (CLUSTER, 5000, watchedEvent -> {
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
        System.out.println(ip + " 成功连接zk");
        /*向服务中心注册
         * 1. 如果创建成功，表示正常注册到服务注册中心，监听需调用服务 列表的变化，及时感知
         *  2.如果创建失败，直接退出
         */
        startRegister(zooKeeper, ip);
    }

    private void startRegister(ZooKeeper zooKeeper, String ip) {
        zooKeeper.create(USER_PATH + "/" + ip, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, (i, s, o, s1) -> {
            if (i == KeeperException.Code.OK.intValue()) {
                System.out.println(ip + " 成功注册到zk");

                try {
                    List<String> firstIps = refreshIps(zooKeeper, ip);
                    if (firstIps.size() < 1) {
                        System.out.println("UserService没有在ip " + ip + " 发现可用节点");
                    } else {
                     firstIps.stream().map(orderIp->"UserService在" +ip +"第一次发现可用节点" +orderIp  ).forEach(System.out::println);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("UserService 状态异常 " + i);
            }
        }, "CTX_DATA");
    }

    private List<String> refreshIps(ZooKeeper zooKeeper, String ip) throws KeeperException, InterruptedException {
        //监听需要调用服务的节点
        return zooKeeper.getChildren(ORDER_PATH , watchedEvent -> {
            // 如果子节点发生变化
            if (watchedEvent.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                //刷新子节点列表 并继续监听父节点
                try {
                    List<String> newIps = refreshIps(zooKeeper, ip);
                    if (newIps.size() < 1) {
                        System.out.println("UserService没有在ip " + ip + " 发现可用节点");
                    } else {
                       newIps.stream().map(orderIp->"UserService在" +ip +" 发现可用节点 " +orderIp).forEach(System.out::println);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("UserService 其他事件" + watchedEvent.getType());
            }
        });
    }
}
