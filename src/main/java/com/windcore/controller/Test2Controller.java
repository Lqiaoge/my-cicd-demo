package com.windcore.controller;

import com.windcore.UserDto;
import com.windcore.service.ValidationGroups;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test2")
public class Test2Controller {

    @PostMapping("/demo")
    public String demo(@Validated @RequestBody UserDto dto) {
        System.out.println(dto);
        return "demo";
    }

    @PostMapping("/validate/{newId}")
    public String validate(@Validated(ValidationGroups.CreateGroup.class) @RequestBody UserDto dto,@PathVariable @Size(min = 3,max = 5,message = "长度必须在3-5之间") String newId) {
        System.out.println(dto);
        System.out.println(newId);
        return "validate";
    }
}
