package com.l.zk.timedtask.order;

import lombok.Data;

/**
 * @author sin97.cn
 * @date 2019/7/24 16:16
 **/
@Data
public class Order {
    private Long orderId;
    private String status;

    public Order(Long orderId, String status) {
        this.orderId = orderId;
        this.status = status;
    }

}
