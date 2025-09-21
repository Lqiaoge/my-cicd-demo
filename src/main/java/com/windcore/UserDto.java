package com.windcore;

import com.windcore.service.ValidationGroups;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    @NotEmpty(groups = ValidationGroups.UpdateGroup.class, message = "用户ID不能为空")
    @Null(groups = ValidationGroups.CreateGroup.class, message = "用户ID要为空")
    private String id;

    @NotEmpty(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20之间")
    private String username;

    @NotEmpty(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$", message = "密码必须包含字母和数字，且长度至少8位")
    private String password;

    @Email(message = "邮箱格式不正确")
    private String email;

    @NotNull
    @Min(value = 1, message = "数量至少1个")
    private Integer count;

    private String code;

    // 如果返回值是false，就是校验失败
    @AssertTrue(message = "当购买数量大于10时必须填写优惠码")
    public boolean isCodeRequired(){
        return count<=10 || code!=null;
    }
}
