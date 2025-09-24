package com.windcore.mapper;

import com.windcore.pojo.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderItemMapper {

    @Select("select * from test.order_item where order_id = #{orderId}")
    List<OrderItem> findOrderItemByOrderId(Long orderId);
}
