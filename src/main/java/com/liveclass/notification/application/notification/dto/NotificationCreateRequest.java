package com.liveclass.notification.application.notification.dto;

import java.util.UUID;

import com.liveclass.notification.domain.notification.NotificationChannel;

public record NotificationCreateRequest(
	UUID userId,
	NotificationChannel channel,
	String eventKey,
	String content
) {
}