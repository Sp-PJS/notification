package com.liveclass.notification.global.exception;

public class NotificationException extends BusinessException {
	public NotificationException(ErrorCode errorCode) {
		super(errorCode);
	}
}
