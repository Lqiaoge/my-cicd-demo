package com.windcore.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3.0 配置类
 * 
 * @author windcore
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * 自定义OpenAPI信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("WindCore Spring Boot Demo API")
                        .description("这是一个基于Spring Boot 3.x的演示项目API文档，包含邮件服务、文件上传、Redis缓存等功能模块。")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("WindCore")
                                .email("windcore@example.com")
                                .url("https://github.com/windcore"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("本地开发环境"),
                        new Server()
                                .url("https://api.windcore.com")
                                .description("生产环境")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Bearer Token认证 - 推荐用于生产环境，提供无状态认证"))
                        .addSecuritySchemes("apiKey", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-Key")
                                .description("API密钥认证 - 适用于服务间调用，请在请求头中添加 X-API-Key"))
                        .addSecuritySchemes("basicAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")
                                .description("基础认证 - 使用用户名和密码进行认证，适用于管理接口")))
                .tags(List.of(
                        new Tag().name("邮件服务").description("邮件发送、状态查询、队列管理等功能"),
                        new Tag().name("文件服务").description("文件分片上传、断点续传、文件管理等功能"),
                        new Tag().name("Redis服务").description("Redis缓存操作、连接测试等功能"),
                        new Tag().name("测试服务").description("文件上传测试、用户信息获取、异步测试等功能"),
                        new Tag().name("验证测试服务").description("数据验证功能测试"),
                        new Tag().name("系统管理").description("系统管理和监控接口")));
    }

    /**
     * 邮件服务API分组
     */
    @Bean
    public GroupedOpenApi mailApi() {
        return GroupedOpenApi.builder()
                .group("mail")
                .displayName("邮件服务")
                .pathsToMatch("/api/mail/**")
                .build();
    }

    /**
     * 文件上传API分组
     */
    @Bean
    public GroupedOpenApi fileApi() {
        return GroupedOpenApi.builder()
                .group("file")
                .displayName("文件服务")
                .pathsToMatch("/api/file/**")
                .build();
    }

    /**
     * Redis服务API分组
     */
    @Bean
    public GroupedOpenApi redisApi() {
        return GroupedOpenApi.builder()
                .group("redis")
                .displayName("Redis服务")
                .pathsToMatch("/api/redis/**")
                .build();
    }

    /**
     * 测试API分组
     */
    @Bean
    public GroupedOpenApi testApi() {
        return GroupedOpenApi.builder()
                .group("test")
                .displayName("测试接口")
                .pathsToMatch("/test/**", "/test2/**")
                .build();
    }

    /**
     * 系统管理API分组
     */
    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("system")
                .displayName("系统管理")
                .pathsToMatch("/actuator/**", "/health/**")
                .build();
    }
}