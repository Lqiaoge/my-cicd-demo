package com.windcore.service.impl;

import com.windcore.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class TestServiceImpl implements TestService {

    @Autowired
    private ApplicationContext context;

    @Override
    public void testAsync() {
        System.out.println("testAsync方法：" + Thread.currentThread().getId());
        // 1.验证非代理调用 同一个线程

        // 2.验证代理调用
        TestService proxy = context.getBean(TestService.class);
        CompletableFuture<String> future = proxy.asyncFn();
//        try {
//            future.get() ;
//        }catch (Exception e){
//            System.out.println(e);
//        }
        future.thenApply(result -> {
            System.out.println("异步方法执行结果：" + result);
            return "ok";
        }).exceptionally(e -> {
            System.out.println("异步方法执行异常：" + e.getMessage());
            return "error";
        });
    }

    @Async
    public CompletableFuture<String> asyncFn() {
        System.out.println("asyncFn方法：" + Thread.currentThread().getId());
        System.out.println("异步方法执行中...");
//        return CompletableFuture.failedFuture(
//                new RuntimeException("异步方法执行异常")
//        );
        return CompletableFuture.supplyAsync(() -> {
            return "异步方法执行完成";
        });
    }
}
