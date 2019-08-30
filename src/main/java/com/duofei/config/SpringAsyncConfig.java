package com.duofei.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * spring 异步线程池配置
 * @author duofei
 * @date 2019/8/20
 */
@Configuration
@EnableAsync
public class SpringAsyncConfig {

    @Bean("eventExecutor")
    public Executor eventExecutor(){
        return new ThreadPoolExecutor(3,5,3, TimeUnit.MINUTES,new LinkedBlockingQueue<>(),r->{
            Thread thread = new Thread(r);
            thread.setName("eventPublishThread");
            return thread;
        });
    }
}
