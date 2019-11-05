package com.l.zk.timedtask.master;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * @author sin97.cn
 * @date 2019/7/24 15:25
 **/
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        IntStream.rangeClosed(1,5)
                .mapToObj(index->"机器"+index)
                .map(TimedTask::new)
                .map(timedTask -> (Runnable) timedTask::go)
                .map(Thread::new)
                .forEach(Thread::start);
        try { TimeUnit.SECONDS.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
    }
}
