package com.example.mail.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
@EnableAsync
public class ExecutorConfig {

    @Value("${async.executor.thread.core_pool_size}")
    private int corePoolSize;
    @Value("${async.executor.thread.max_pool_size}")
    private int maxPoolSize;
    @Value("${async.executor.thread.queue_capacity}")
    private int queueCapacity;
    @Value("${async.executor.thread.keep_alive_seconds}")
    private int keepAliveSeconds;
    @Value("${async.executor.thread.await_termination_seconds}")
    private int awaitTerminationSeconds;
    @Value("${async.executor.thread.name.prefix}")
    private String namePrefix;
    @Value("${async.executor.thread.name.prefixson}")
    private String namePrefixSon;

    @Bean(name = "asyncServiceExecutor")
    public Executor asyncServiceExecutor() {
        log.info("start asyncServiceExecutor");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心线程数量，线程池创建时候初始化的线程数
        executor.setCorePoolSize(corePoolSize);
        //最大线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
        executor.setMaxPoolSize(maxPoolSize);
        //缓冲队列，用来缓冲执行任务的队列
        executor.setQueueCapacity(queueCapacity);
        //当超过了核心线程出之外的线程在空闲时间到达之后会被销毁
        executor.setKeepAliveSeconds(keepAliveSeconds);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix(namePrefix);
        //设置线程池中任务的等待时间，如果超过这个时候还没有销毁就强制销毁，以确保应用最后能够被关闭，而不是阻塞住。
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
        // rejection-policy：当pool已经达到max size的时候，如何处理新任务
        // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行;
        // 线程池对拒绝任务的处理策略：这里采用了CallerRunsPolicy策略，当线程池没有处理能力的时候，该策略会直接在 execute 方法的调用线程中运行被拒绝的任务；如果执行程序已关闭，则会丢弃该任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        //执行初始化
        executor.initialize();
        return executor;
    }
    @Bean(name = "asyncServiceSonExecutor")
    public Executor asyncServiceSonExecutor() {
        log.info("start asyncServiceSonExecutor");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心线程数量，线程池创建时候初始化的线程数
        executor.setCorePoolSize(corePoolSize);
        //最大线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
        executor.setMaxPoolSize(maxPoolSize);
        //缓冲队列，用来缓冲执行任务的队列
        executor.setQueueCapacity(queueCapacity);
        //当超过了核心线程出之外的线程在空闲时间到达之后会被销毁
        executor.setKeepAliveSeconds(keepAliveSeconds);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix(namePrefixSon);
        //设置线程池中任务的等待时间，如果超过这个时候还没有销毁就强制销毁，以确保应用最后能够被关闭，而不是阻塞住。
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
        // rejection-policy：当pool已经达到max size的时候，如何处理新任务
        // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行;
        // 线程池对拒绝任务的处理策略：这里采用了CallerRunsPolicy策略，当线程池没有处理能力的时候，该策略会直接在 execute 方法的调用线程中运行被拒绝的任务；如果执行程序已关闭，则会丢弃该任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        //执行初始化
        executor.initialize();
        return executor;
    }
}
