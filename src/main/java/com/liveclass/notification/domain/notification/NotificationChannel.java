package com.liveclass.notification.domain.notification;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum NotificationChannel {
	EMAIL,
	IN_APP
}
