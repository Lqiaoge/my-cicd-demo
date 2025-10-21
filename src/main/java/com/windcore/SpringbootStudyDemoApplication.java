package com.windcore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SpringbootStudyDemoApplication {



    public static void main(String[] args) {
        SpringApplication.run(SpringbootStudyDemoApplication.class, args);
    }



}
