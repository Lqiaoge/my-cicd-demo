package com.windcore.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户信息")
public class User {

    @Schema(description = "用户姓名", example = "张三", required = true)
    private String name;

    @Schema(description = "用户年龄", example = "25", minimum = "0", maximum = "150")
    private Integer age;

}
