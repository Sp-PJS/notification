package com.liveclass.notification.infrastructure.sse;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class SseConfig {

	@Bean(name = "sseExecutor")
	public Executor sseExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(10);   // 기본 유지할 스레드 수
		executor.setMaxPoolSize(50);    // 최대 확장 스레드 수
		executor.setQueueCapacity(100); // 버퍼 큐 용량
		executor.setThreadNamePrefix("sse-async-");
		executor.initialize();
		return executor;
	}
}
