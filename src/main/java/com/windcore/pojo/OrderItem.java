package com.windcore.pojo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItem {

    private Long id;

    private Long orderId;

    private String productName;

    private BigDecimal price;
}
