package com.l.zk.timedtask.order;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author sin97.cn
 * @date 2019/7/24 16:13
 **/
public class Job {
    private static final String CONNECT = "94.191.56.73:2181,94.191.56.73:2182,94.191.56.73:2183";
    private static final String LOCK_PREFIX = "/orderLock/";

    public void start() {
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
        System.out.println("Job >>>>>>>成功连接zk");


        startUpdate(zooKeeper);

    }

    private void startUpdate(ZooKeeper zooKeeper) {
    /*
       1.查询出未支付的订单（从数据库中），         list模拟
       2.处理订单的时候加锁，
       3.创建成功，执行业务逻辑
       4.创建失败，跳过
     */
        List<Order> orderList = Arrays.asList(new Order(1L, "NO_PAY"), new Order(2L, "NO_PAY"));

        Iterator<Order> iterator = orderList.iterator();
        while (iterator.hasNext()) {
            Order order = iterator.next();
            String lockPath = LOCK_PREFIX + order.getOrderId();
            zooKeeper.create(lockPath, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, (i, s, o, s1) -> {
                if (i == KeeperException.Code.OK.intValue()) {
                    System.out.println("Job开始执行业务逻辑处理>>>>> 订单id: "+order.getOrderId());

                    try {
                        TimeUnit.SECONDS.sleep(3);
                        zooKeeper.delete(lockPath, -1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    System.out.println("Job处理完毕>>>>> 订单id: "+order.getOrderId());
                } else if (i == KeeperException.Code.NODEEXISTS.intValue()) {
                    System.out.println("Job跳过>>>>> 订单id: "+order.getOrderId());
                    iterator.remove();

                } else {
                    System.out.println(order.getOrderId()+">>>>> 订单处理异常"+">>>>>>"+i);
                }


            }, "data");
        }

    }
}
