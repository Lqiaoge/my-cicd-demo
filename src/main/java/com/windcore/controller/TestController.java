package com.windcore.controller;


import com.windcore.pojo.User;

import com.windcore.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class TestController {


    @Autowired
    private TestService testService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @CrossOrigin
    public String upload(
            @RequestPart(name = "file") MultipartFile file,
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
    @PostMapping(value = "/upload2", consumes = "multipart/form-data")
    @CrossOrigin
    public String upload2(
            @RequestPart(name = "file") MultipartFile file,
            @RequestPart(name = "user") User user
    ) {
        System.out.println("文件名: " + file.getOriginalFilename());
        System.out.println("文件大小: " + file.getSize() + " bytes");
        System.out.println("用户信息: " + user.getName() + ", 年龄: " + user.getAge());

        return "上传成功 - 文件: " + file.getOriginalFilename() + ", 用户: " + user.getName();
    }

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

    @GetMapping("/testAsync")
    public boolean testAsync() {
        testService.testAsync();
        return true;
    }

}
