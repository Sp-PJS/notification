package com.liveclass.notification.application.notification.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.liveclass.notification.domain.notification.NotificationChannel;
import com.liveclass.notification.domain.notification.NotificationStatus;

public record NotificationStatusResponse(
	NotificationStatus status
) {
}
