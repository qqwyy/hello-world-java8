package com.completableFuture;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class completableFutureTest {

    @Test
    public void testThen() throws ExecutionException, InterruptedException {
        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> {
            return "zero";
        });

        CompletableFuture<Integer> f2 = f1.thenApply(new Function<String, Integer>() {

            @Override
            public Integer apply(String t) {
                System.out.println(2);
                return Integer.valueOf(t.length());
            }
        });

        CompletableFuture<Double> f3 = f2.thenApply(r -> r * 2.0);
        System.out.println(f3.get());
    }

}
