package com.liveclass.notification.application.notification.port;

import com.liveclass.notification.application.notification.dto.NotificationCreateRequest;

public interface NotificationSenderPort {
	void sendInApp(NotificationCreateRequest request);
	void sendEmail(NotificationCreateRequest request);
}