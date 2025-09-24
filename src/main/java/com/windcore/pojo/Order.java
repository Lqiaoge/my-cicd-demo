package com.windcore.pojo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Order {

    private Long id;

    private String orderNo;

    private LocalDateTime createTime;

    private List<OrderItem> orderItems;
}
