package com.liveclass.notification.infrastructure.messaging;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.liveclass.notification.application.notification.dto.NotificationPendingEvent;
import com.liveclass.notification.application.notification.port.NotificationPublisherPort;
import com.liveclass.notification.application.registration.CourseRegistrationEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SpringEventNotificationPublisherAdapter implements NotificationPublisherPort {

	private final ApplicationEventPublisher applicationEventPublisher;

	// 수강신청 / 취소 이벤트 발행
	@Override
	public void publish(CourseRegistrationEvent event) {
		// 내부 스프링 이벤트 채널로 전달 (메시지 브로커 전환 가능 구조)
		applicationEventPublisher.publishEvent(event);
	}

	// 알림 발송 요청 등록 이벤트 발행
	@Override
	public void publish(NotificationPendingEvent event) {
		// 내부 스프링 이벤트 채널로 전달 (메시지 브로커 전환 가능 구조)
		applicationEventPublisher.publishEvent(event);
	}
}