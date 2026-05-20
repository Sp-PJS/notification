package com.liveclass.notification.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync // 비동기 기능 활성화
public class AsyncEventConfig {

	@Bean(name = "eventExecutor")
	public Executor eventExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);          // 기본 스레드 수
		executor.setMaxPoolSize(20);         // 최대 스레드 수
		executor.setQueueCapacity(500);      // 대기열 용량
		executor.setThreadNamePrefix("event-async-");
		executor.initialize();
		return executor;
	}
}