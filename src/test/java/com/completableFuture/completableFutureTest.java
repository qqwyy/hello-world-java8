package com.completableFuture;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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



    /**
     * future完成处理,可获取结果
     */
    @Test
    public void testThenAccept(){
        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> {
            return "zero";
        });
        f1.thenAccept(e -> {
            System.out.println("get result:"+e);
        });
    }

    /**
     * future完成处理
     */
    @Test
    public void testThenRun(){
        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> {
            return "zero";
        });
        f1.thenRun(new Runnable() {
            @Override
            public void run() {
                System.out.println("finished");
            }
        });
    }


    /**
     * compose相当于flatMap,避免CompletableFuture<CompletableFuture<String>>这种
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testThenCompose() throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> {
            return "zero";
        }, executor);
        CompletableFuture<CompletableFuture<String>> f4 = f1.thenApply(completableFutureTest::calculate);
        System.out.println("f4.get:"+f4.get().get());

        CompletableFuture<String> f5 = f1.thenCompose(completableFutureTest::calculate);
        System.out.println("f5.get:"+f5.get());

        System.out.println(f1.get());
    }

    public static CompletableFuture<String> calculate(String input) {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println(input);
            return input + "---" + input.length();
        }, executor);
        return future;
    }



    /**
     * thenCombine用于组合两个并发的任务,产生新的future有返回值
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testThenCombine() throws ExecutionException, InterruptedException {
        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("f1 start to sleep at:"+System.currentTimeMillis());
                Thread.sleep(1000);
                System.out.println("f1 finish sleep at:"+System.currentTimeMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "zero ";
        });
        CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("f2 start to sleep at:"+System.currentTimeMillis());
                Thread.sleep(3000);
                System.out.println("f2 finish sleep at:"+System.currentTimeMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "hello ";
        });

        CompletableFuture<String> reslutFuture =
                f1.thenCombine(f2, new BiFunction<String, String, String>() {

                    @Override
                    public String apply(String t, String u) {
                        System.out.println("f3 start to combine at:"+System.currentTimeMillis());
                        return t.concat(u);
                    }
                });

        System.out.println(reslutFuture.get());//zerohello
        System.out.println("finish combine at:"+System.currentTimeMillis());
    }



    /**
     * thenAcceptBoth用于组合两个并发的任务,产生新的future没有返回值
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testThenAcceptBoth() throws ExecutionException, InterruptedException {
        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("f1 start to sleep at:"+System.currentTimeMillis());
                TimeUnit.SECONDS.sleep(1);
                System.out.println("f1 stop sleep at:"+System.currentTimeMillis());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "zero";
        });
        CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("f2 start to sleep at:"+System.currentTimeMillis());
                TimeUnit.SECONDS.sleep(3);
                System.out.println("f2 stop sleep at:"+System.currentTimeMillis());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "hello";
        });

        CompletableFuture<Void> reslutFuture = f1.thenAcceptBoth(f2, new BiConsumer<String, String>() {
            @Override
            public void accept(String t, String u) {
                System.out.println("f3 start to accept at:"+System.currentTimeMillis());
                System.out.println(t + " over");
                System.out.println(u + " over");
            }
        });

        System.out.println(reslutFuture.get());
        System.out.println("finish accept at:"+System.currentTimeMillis());

    }



    /**
     * 当任意一个CompletionStage 完成的时候，fn 会被执行,它的返回值会当做新的CompletableFuture<U>的计算结果
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testApplyToEither() throws ExecutionException, InterruptedException {
        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("f1 start to sleep at:"+System.currentTimeMillis());
                TimeUnit.SECONDS.sleep(5);
                System.out.println("f1 stop sleep at:"+System.currentTimeMillis());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "fromF1";
        });
        CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("f2 start to sleep at:"+System.currentTimeMillis());
                TimeUnit.SECONDS.sleep(10);
                System.out.println("f2 stop sleep at:"+System.currentTimeMillis());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "fromF2";
        });

        CompletableFuture<String> reslutFuture = f1.applyToEither(f2,i -> i.toString());
        System.out.println(reslutFuture.get()); //should not be null , wait for complete
    }



    /**
     * 取其中返回最快的一个
     * 当任意一个CompletionStage 完成的时候，action 这个消费者就会被执行。这个方法返回 CompletableFuture<Void>
     */
    @Test
    public void testAcceptEither() throws ExecutionException, InterruptedException {
        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("f1 start to sleep at:"+System.currentTimeMillis());
                TimeUnit.SECONDS.sleep(3);
                System.out.println("f1 stop sleep at:"+System.currentTimeMillis());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "zero";
        });
        CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("f2 start to sleep at:"+System.currentTimeMillis());
                TimeUnit.SECONDS.sleep(5);
                System.out.println("f2 stop sleep at:"+System.currentTimeMillis());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "hello";
        });

        CompletableFuture<Void> reslutFuture = f1.acceptEither(f2,r -> {
            System.out.println("quicker result:"+r);
        });
        reslutFuture.get(); //should be null , wait for complete

    }


    /**
     * 等待多个future返回
     */
    @Test
    public void testAllOf() throws InterruptedException {
        List<CompletableFuture<String>> futures = IntStream.range(1,10)
                .mapToObj(i ->
                        longCost(i)).collect(Collectors.toList());
        final CompletableFuture<Void> allCompleted = CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{}));
        //        try {
//            allCompleted.get();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }

        allCompleted.thenRun(() -> {
            futures.stream().forEach(future -> {
                try {
                    System.out.println("get future at:"+System.currentTimeMillis()+", result:"+future.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });

        });

        //需要再主线程get或join 获取对应值
        Thread.sleep(10000); //wait
    }


//    public static CompletableFuture<String> longCost(int input) {
//        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//            try {
//                TimeUnit.SECONDS.sleep(2);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            return input + "---";
//        });
//        return future;
//    }


    /**
     * 等待多个future当中最快的一个返回
     * @throws InterruptedException
     */
    @Test
    public void testAnyOf() throws InterruptedException {
        List<CompletableFuture<String>> futures = IntStream.range(1,10)
                .mapToObj(i ->
                        longCost(i)).collect(Collectors.toList());
        CompletableFuture<Object> firstCompleted = CompletableFuture.anyOf(futures.toArray(new CompletableFuture[]{}));
        try {
            firstCompleted.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        firstCompleted.thenAccept((Object result) -> {
            System.out.println("get at:"+System.currentTimeMillis()+",first result:"+result);
        });

        //需要再主线程get或join 获取对应值
    }

    private CompletableFuture<String> longCost(long i){
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("f"+i+" start to sleep at:"+System.currentTimeMillis());
                Thread.sleep(3000);
                System.out.println("f"+i+" stop sleep at:"+System.currentTimeMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return String.valueOf(i);
        });
    }

}
