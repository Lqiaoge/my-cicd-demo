package com.windcore.service;

import org.apache.ibatis.jdbc.SQL;

public class UserSqlProvider {

    public String findUserByCondition(String name, String password) {
        return new SQL() {{
            SELECT("*");
            FROM("test_user");
            if (name != null && !name.isEmpty()) {
                WHERE("name = #{name}");
            }
            if (password != null && !password.isEmpty()) {
                AND().WHERE("password = #{password}");
            }
        }}.toString();
    }
}
