package com.windcore.service;

import java.util.concurrent.CompletableFuture;

public interface TestService {

    void testAsync();

    CompletableFuture<String> asyncFn();
}
