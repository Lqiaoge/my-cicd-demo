package com.windcore.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "demo.test")
@Component
public class TestProperties {

    private String name;
    private int age;
    private String param;
    private String appName;
    private Info info;

    @Data
    public static class Info {
        private String phone;
        private String email;
    }
}
