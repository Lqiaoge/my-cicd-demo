package com.windcore.mapper;

import com.windcore.pojo.Order;
import org.apache.ibatis.annotations.*;

@Mapper
public interface OrderMapper {


    @Select("select * from test.order where id = #{id}")
    @Results({
            @Result(property = "id",column = "id"),
            @Result(property = "orderNo",column = "order_no"),
            @Result(property = "createTime",column = "create_time"),
            @Result(property = "orderItems",column = "id",many = @Many(select = "com.windcore.mapper.OrderItemMapper.findOrderItemByOrderId"))
    })
    Order findOrderById(Long id);
}
