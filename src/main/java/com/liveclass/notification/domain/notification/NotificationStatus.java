package com.liveclass.notification.domain.notification;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum NotificationStatus {
	PENDING,    // 발송 대기
	PROCESSING, // 처리 중
	SUCCESS,    // 발송 성공
	FAILED,      // 발송 실패
	FAILED_PERMANENT // 최종 실패
}
