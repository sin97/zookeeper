package com.l.zk.timedtask.order;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author sin97.cn
 * @date 2019/7/24 15:46
 **/
public class PayOrder {
    private static final String CONNECT = "94.191.56.73:2181,94.191.56.73:2182,94.191.56.73:2183";
    private static final String LOCK_PREFIX = "/orderLock/";

    public void start(Long orderId) {
        CountDownLatch latch = new CountDownLatch(1);
        ZooKeeper zooKeeper = null;
        try {
            zooKeeper = new ZooKeeper(PayOrder.CONNECT, 5000, watchedEvent -> {
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
        System.out.println("PayOrder >>>>>>>成功连接zk");

        System.out.println("尝试支付>>>>>>>>>>>>>>>"+orderId);
        String status = startPay(zooKeeper, orderId);

        System.out.println("订单" + orderId + "状态为:" + status);
    }

    private String startPay(ZooKeeper zooKeeper, Long orderId) {
        String lockPath = LOCK_PREFIX + orderId;
        //支付之前加锁 ，创建临时无序节点
        try {
            zooKeeper.create(lockPath, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            try {
                TimeUnit.SECONDS.sleep(3);
                zooKeeper.delete(lockPath, -1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("订单" + orderId + "支付成功");
            return "success";
        } catch (KeeperException.NodeExistsException e) {
            System.out.println("订单" + orderId + "已加锁, 用户操作失败，请稍后重试");
            return "fail";
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("订单" + orderId + "状态异常");
            return "error";
        }


    }

}
