package com.liveclass.notification.application.notification.dto;

import java.util.UUID;

public record NotificationPendingEvent(
	UUID notificationId
) {
}
