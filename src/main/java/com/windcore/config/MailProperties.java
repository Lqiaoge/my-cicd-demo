package com.windcore.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * 邮件配置属性类
 * 
 * @author windcore
 */
@Data
@Component
@ConfigurationProperties(prefix = "mail")
public class MailProperties {

    /**
     * SMTP配置
     */
    private Smtp smtp = new Smtp();

    /**
     * 模板配置
     */
    private Template template = new Template();

    /**
     * 发送方配置
     */
    private Sender sender = new Sender();

    /**
     * 异步配置
     */
    private Async async = new Async();

    @Data
    public static class Smtp {
        private String host = "smtp.qq.com";
        private Integer port = 587;
        private String username;
        private String password;
        private String protocol = "smtp";
        private String defaultEncoding = "UTF-8";
        private Properties properties = new Properties();
    }

    @Data
    public static class Template {
        private Boolean cache = true;
        private String encoding = "UTF-8";
        private String mode = "HTML";
        private String prefix = "classpath:/templates/mail/";
        private String suffix = ".html";
    }

    @Data
    public static class Sender {
        private String name = "企业邮件服务";
        private String from;
    }

    @Data
    public static class Async {
        private Integer corePoolSize = 5;
        private Integer maxPoolSize = 10;
        private Integer queueCapacity = 100;
        private String threadNamePrefix = "mail-async-";
    }
}