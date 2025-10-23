package com.windcore.controller;

import com.windcore.UserDto;
import com.windcore.service.ValidationGroups;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test2")
@Tag(name = "验证测试服务", description = "提供数据验证功能测试")
public class Test2Controller {

    @Operation(
        summary = "基础验证测试",
        description = "测试基础的数据验证功能",
        tags = {"数据验证"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "验证成功"),
        @ApiResponse(responseCode = "400", description = "验证失败")
    })
    @PostMapping("/demo")
    public String demo(
        @Parameter(description = "用户数据传输对象", required = true)
        @Validated @RequestBody UserDto dto) {
        System.out.println(dto);
        return "demo";
    }

    @Operation(
        summary = "分组验证测试",
        description = "测试分组验证功能，包括路径参数验证",
        tags = {"数据验证"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "验证成功"),
        @ApiResponse(responseCode = "400", description = "验证失败")
    })
    @PostMapping("/validate/{newId}")
    public String validate(
        @Parameter(description = "用户数据传输对象", required = true)
        @Validated(ValidationGroups.CreateGroup.class) @RequestBody UserDto dto,
        @Parameter(description = "新ID，长度必须在3-5之间", required = true, example = "abc")
        @PathVariable @Size(min = 3,max = 5,message = "长度必须在3-5之间") String newId) {
        System.out.println(dto);
        System.out.println(newId);
        return "validate";
    }
}
