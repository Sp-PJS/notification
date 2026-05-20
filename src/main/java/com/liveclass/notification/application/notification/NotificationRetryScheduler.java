package com.liveclass.notification.application.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRetryScheduler {

	private final NotificationRecoveryService notificationRecoveryService;

	// 1분마다 실패(FAILED) 상태의 알림들을 모아 재시도 수행
	@Scheduled(fixedDelay = 60000)
	public void retryFailedNotifications() {
		log.info("[배치] 실패 알림 재시도 스케줄러 가동 시작");

		try {
			notificationRecoveryService.recoverFailedNotifications();
			log.info("[배치 완료] 실패 알림 재시도 스케줄러 가동 정상 종료");
		} catch (Exception e) {
			log.error("[배치 장애] 알림 재시도 배치 처리 중 예외 발생", e);
		}
	}
}