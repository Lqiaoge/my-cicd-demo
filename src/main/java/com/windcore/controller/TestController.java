package com.windcore.controller;

import com.windcore.pojo.User;
import com.windcore.service.TestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@Tag(name = "测试服务", description = "提供文件上传、用户信息获取、异步测试等功能")
public class TestController {


    @Autowired
    private TestService testService;

    @Operation(
        summary = "文件上传（JSON字符串方式）",
        description = "上传文件并传递用户信息（用户信息以JSON字符串形式传递）",
        tags = {"文件上传"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "上传成功"),
        @ApiResponse(responseCode = "400", description = "上传失败")
    })
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @CrossOrigin
    public String upload(
            @Parameter(description = "上传的文件", required = true)
            @RequestPart(name = "file") MultipartFile file,
            @Parameter(description = "用户信息JSON字符串", required = true, 
                      example = "{\"name\":\"张三\",\"age\":25}")
            @RequestParam(name = "user") String userJson
    ) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            User user = objectMapper.readValue(userJson, User.class);

            System.out.println("文件名: " + file.getOriginalFilename());
            System.out.println("文件大小: " + file.getSize() + " bytes");
            System.out.println("用户信息: " + user.getName() + ", 年龄: " + user.getAge());

            return "上传成功 - 文件: " + file.getOriginalFilename() + ", 用户: " + user.getName();
        } catch (Exception e) {
            e.printStackTrace();
            return "上传失败: " + e.getMessage();
        }
    }

    // 替代方案：使用RequestPart接收User对象（需要前端发送JSON Blob）
    @Operation(
        summary = "文件上传（对象方式）",
        description = "上传文件并传递用户信息（用户信息以JSON对象形式传递）",
        tags = {"文件上传"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "上传成功"),
        @ApiResponse(responseCode = "400", description = "上传失败")
    })
    @PostMapping(value = "/upload2", consumes = "multipart/form-data")
    @CrossOrigin
    public String upload2(
            @Parameter(description = "上传的文件", required = true)
            @RequestPart(name = "file") MultipartFile file,
            @Parameter(description = "用户信息对象", required = true)
            @RequestPart(name = "user") User user
    ) {
        System.out.println("文件名: " + file.getOriginalFilename());
        System.out.println("文件大小: " + file.getSize() + " bytes");
        System.out.println("用户信息: " + user.getName() + ", 年龄: " + user.getAge());

        return "上传成功 - 文件: " + file.getOriginalFilename() + ", 用户: " + user.getName();
    }

    @Operation(
        summary = "获取用户信息",
        description = "获取示例用户信息，支持JSON、XML、YAML多种格式输出",
        tags = {"用户信息"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "成功获取用户信息",
            content = {
                @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                        schema = @Schema(implementation = User.class)),
                @Content(mediaType = MediaType.APPLICATION_XML_VALUE, 
                        schema = @Schema(implementation = User.class))
            }
        )
    })
    @GetMapping(value = "/user", produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_YAML_VALUE  // 新增 YAML 格式
    })
    public User getUser() {
        User user = new User();
        user.setName("张三");
        user.setAge(20);
        return user;
    }

    @Operation(
        summary = "测试异步功能",
        description = "测试系统的异步处理功能",
        tags = {"系统测试"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "异步测试启动成功")
    })
    @GetMapping("/testAsync")
    public boolean testAsync() {
        testService.testAsync();
        return true;
    }

}
