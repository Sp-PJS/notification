package com.liveclass.notification.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing // 메인 클래스에 직접 설정하면 테스트 격리 시 불편하므로 별도로 설정
public class JpaAuditingConfig {
}
