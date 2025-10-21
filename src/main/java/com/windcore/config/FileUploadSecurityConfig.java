package com.windcore.config;


import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class FileUploadSecurityConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        // 单个文件大小限制
        factory.setMaxFileSize(DataSize.ofMegabytes(50));
        // 总请求大小限制
        factory.setMaxRequestSize(DataSize.ofMegabytes(100));
        // 临时文件存储路径
        factory.setLocation("D:/tmp");
        return factory.createMultipartConfig();
    }


}
