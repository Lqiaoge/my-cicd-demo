package com.windcore.mapper;

import com.windcore.pojo.User;
import com.windcore.service.UserSqlProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

@Mapper
public interface UserMapper {

    @SelectProvider(type = UserSqlProvider.class, method = "findUserByCondition")
    List<User> findUserByCondition(String name, String password);
}
