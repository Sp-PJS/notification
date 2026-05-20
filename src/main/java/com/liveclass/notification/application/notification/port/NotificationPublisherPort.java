package com.liveclass.notification.application.notification.port;

import com.liveclass.notification.application.notification.dto.NotificationPendingEvent;
import com.liveclass.notification.application.registration.CourseRegistrationEvent;

public interface NotificationPublisherPort {

	// 수강신청 / 취소 이벤트 발행
	void publish(CourseRegistrationEvent event);

	// 알림 발송 요청 등록 이벤트 발행
	void publish(NotificationPendingEvent event);
}