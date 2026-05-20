package com.liveclass.notification.application.notification.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.liveclass.notification.domain.notification.NotificationChannel;
import com.liveclass.notification.domain.notification.NotificationStatus;

public record NotificationResponse(
	UUID notificationId,
	NotificationChannel channel,
	NotificationStatus status,
	String content,
	boolean isRead,                // 읽음 여부
	LocalDateTime createdAt
) {
	
}