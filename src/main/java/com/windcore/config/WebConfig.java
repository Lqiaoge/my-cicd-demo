package com.windcore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 
 * @author windcore
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置静态资源处理
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置Swagger UI静态资源
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/")
                .resourceChain(false);
        
        // 配置自定义静态资源
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(false);
    }

    /**
     * 配置视图控制器
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 将根路径重定向到API文档中心
        registry.addRedirectViewController("/", "/api-docs.html");
        registry.addRedirectViewController("/docs", "/api-docs.html");
        // 保留直接访问Swagger UI的路径
        registry.addRedirectViewController("/swagger", "/swagger-ui.html");
    }
}