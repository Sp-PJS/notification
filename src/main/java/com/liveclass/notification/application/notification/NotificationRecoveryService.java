package com.liveclass.notification.application.notification;

import com.liveclass.notification.application.notification.dto.NotificationCreateRequest;
import com.liveclass.notification.application.notification.port.NotificationRepositoryPort;
import com.liveclass.notification.domain.notification.Notification;
import com.liveclass.notification.domain.notification.NotificationStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationRecoveryService {

	private final NotificationRepositoryPort notificationRepositoryPort;
	private final NotificationService notificationService;

	private static final int MAX_RETRY_LIMIT = 4; // 최대 재시도 제약 수치

	// FAILED 상태이고, 최대 재시도 횟수를 넘지 않은 알림들을 복구 타겟으로 잡아 재전송 처리

	public void recoverFailedNotifications() {

		List<Notification> targetNotifications = notificationRepositoryPort
			.findAllByStatusAndRetryCountLessThan(NotificationStatus.FAILED, MAX_RETRY_LIMIT);

		if (targetNotifications.isEmpty()) {
			log.info("[복구 서비스] 재시도 대상 알림이 존재하지 않습니다.");
			return;
		}

		log.info("[복구 서비스] 총 {}건의 실패 알림 재시도를 시작합니다.", targetNotifications.size());

		for (Notification notification : targetNotifications) {
			try {
				notificationService.PendingNotification(notification.getId());

				log.info("알림 재시도 발송: 수강신청 처리 과정에 지연이 발생하여 알림을 재발송합니다. & userId: {}", notification.getUserId());

			} catch (Exception e) {
				log.warn("[복구 실패] 알림 ID: {} 재시도 중 다시 실패 처리됨", notification.getId());

				// 💡 외부 클래스의 별도 트랜잭션(REQUIRES_NEW) 메서드를 호출하여
				// 💡 실패 카운트와 상태 변경이 전체 롤백에 휘쓸리지 않고 DB에 확실히 기록되게 합니다.
				notificationService.handleRecoveryFailure(notification.getId(), e.getMessage(), MAX_RETRY_LIMIT);
			}
		}
	}
}