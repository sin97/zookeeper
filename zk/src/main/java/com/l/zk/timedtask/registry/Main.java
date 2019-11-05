package com.l.zk.timedtask.registry;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author LJQ
 * @date 2019/7/25 11:04
 **/
public class Main {
    public static void main(String[] args) {
        Arrays.asList("192.168.100.10","192.168.100.11")
                .stream()
                .map(ip->(Runnable)()->new UserService().start(ip))
                .map(Thread::new)
                .forEach(Thread::start);
        try { TimeUnit.SECONDS.sleep(2*60*60*6); } catch (InterruptedException e) { e.printStackTrace(); }
    }
}
