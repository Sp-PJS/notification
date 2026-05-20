package com.liveclass.notification.infrastructure.messaging;

import com.liveclass.notification.application.notification.NotificationService;
import com.liveclass.notification.application.notification.dto.NotificationCreateRequest;
import com.liveclass.notification.application.notification.dto.NotificationPendingEvent;
import com.liveclass.notification.application.registration.CourseRegistrationEvent;
import com.liveclass.notification.domain.notification.NotificationChannel;
import com.liveclass.notification.domain.registration.RegistrationStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

	private final NotificationService notificationService;

	// 수강 신청 / 취소 이벤트를 가로채 비동기 처리

	@Async("eventExecutor") // AsyncConfig 풀 이용
	@EventListener
	public void handleRegistrationEvent(CourseRegistrationEvent event) {
		log.info("[비동기 이벤트 소비] 스레드명: {}, 대상 유저: {}", Thread.currentThread().getName(), event.userId());

		String prefix = (event.status() == RegistrationStatus.COMPLETED) ? "REG_COMP_" : "REG_CAN_";
		String eventKey = prefix + event.userId() + "_" + event.courseId();

		NotificationChannel channel = (event.status() == RegistrationStatus.COMPLETED)
			? NotificationChannel.IN_APP : NotificationChannel.EMAIL;

		String content = (event.status() == RegistrationStatus.COMPLETED)
			? "신청하신 강의 수강신청이 완료되었습니다."
			: "신청하신 강의 수강신청이 정상 취소되었습니다.";

		NotificationCreateRequest createRequest = new NotificationCreateRequest(
			event.userId(),
			channel,
			eventKey,
			"\"수강신청 변동 알림: \"" + content
		);

		// 발송 처리를 위해 NotificationService 호출
		notificationService.processNotification(createRequest);
	}

	// 알림 발송 요청 등록 API 이벤트를 가로채 비동기 처리
	@Async("eventExecutor") // AsyncConfig 풀 이용
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // DB 커밋 완료 후에 처리
	public void handleNotificationPendingEvent(NotificationPendingEvent event) {
		log.info("[비동기 API 알림 발송 소비] 스레드명: {}, 알림 ID: {}", Thread.currentThread().getName(), event.notificationId());

		// DB 저장된 알림 ID를 기반으로 발송 처리(상태 변경)를 위해 NotificationService 호출
		notificationService.PendingNotification(event.notificationId());
	}
}